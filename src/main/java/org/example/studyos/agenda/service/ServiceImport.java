package org.example.studyos.agenda.service;

import org.example.studyos.agenda.depot.ImportAgendaRepository;
import org.example.studyos.agenda.depot.SeanceRepository;
import org.example.studyos.agenda.domaine.ImportAgenda;
import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.agenda.domaine.Seance;
import org.example.studyos.agenda.domaine.TypeSeance;
import org.example.studyos.agenda.ingestion.ClassificateurNature;
import org.example.studyos.agenda.ingestion.DetecteurTypeSeance;
import org.example.studyos.agenda.ingestion.Nature;
import org.example.studyos.agenda.ingestion.NormalisateurLibelle;
import org.example.studyos.agenda.ingestion.NormalisateurSalle;
import org.example.studyos.agenda.ingestion.OccurrenceBrute;
import org.example.studyos.agenda.ingestion.ResultatImport;
import org.example.studyos.agenda.ingestion.SourceAgenda;
import org.example.studyos.suivi.depot.EcheanceRepository;
import org.example.studyos.suivi.domaine.Echeance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Orchestre un import d'agenda (brief §5), dans l'ordre :
 * empreinte -> developpement -> classification -> normalisation -> resolution
 * matiere -> upsert idempotent (uid_externe, debut) -> detection des annulations.
 *
 * Transactionnel : tout l'import reussit ou echoue en bloc.
 */
@Service
public class ServiceImport {

    private static final String SOURCE = "ICS";

    private final ImportAgendaRepository imports;
    private final SeanceRepository seances;
    private final EcheanceRepository echeances;
    private final SourceAgenda source;
    private final ResolveurMatiere resolveur;

    public ServiceImport(ImportAgendaRepository imports,
                         SeanceRepository seances,
                         EcheanceRepository echeances,
                         SourceAgenda source,
                         ResolveurMatiere resolveur) {
        this.imports = imports;
        this.seances = seances;
        this.echeances = echeances;
        this.source = source;
        this.resolveur = resolveur;
    }

    @Transactional
    public ResultatImport importer(String nomFichier, InputStream flux) {
        byte[] contenu = lireTout(flux);
        String hash = sha256(contenu);

        // 1. Empreinte : import deja vu -> ne rien faire (brief §5.1).
        if (imports.findByHashFichier(hash).isPresent()) {
            return ResultatImport.inchange();
        }

        ImportAgenda importAgenda = imports.save(new ImportAgenda(SOURCE, nomFichier, hash));

        // 2. Developpement des occurrences (RRULE/EXDATE/RECURRENCE-ID).
        List<OccurrenceBrute> occurrences = source.lire(new ByteArrayInputStream(contenu));

        // Cache par run : une seule matiere par libelle normalise.
        Map<String, Matiere> matieresParNorm = new HashMap<>();
        // Cles de seances vues dans ce flux, pour la detection des annulations.
        Set<CleOccurrence> seancesVues = new HashSet<>();

        for (OccurrenceBrute occurrence : occurrences) {
            Nature nature = ClassificateurNature.classer(occurrence.libelleBrut(), occurrence.journeeEntiere());
            switch (nature) {
                case REPERE -> { /* ignore au lot 1 */ }
                case ECHEANCE -> traiterEcheance(occurrence, importAgenda, matieresParNorm);
                case SEANCE -> {
                    traiterSeance(occurrence, importAgenda, matieresParNorm);
                    seancesVues.add(new CleOccurrence(occurrence.uidExterne(), occurrence.debut()));
                }
            }
        }

        // 9. Annulations : seances futures d'un import anterieur absentes du flux.
        detecterAnnulations(importAgenda, seancesVues);

        return new ResultatImport(false,
                importAgenda.getNbCrees(),
                importAgenda.getNbModifies(),
                importAgenda.getNbInchanges(),
                importAgenda.getNbAnnules());
    }

    private void traiterSeance(OccurrenceBrute occurrence, ImportAgenda importAgenda,
                               Map<String, Matiere> cache) {
        String libelleNorm = NormalisateurLibelle.normaliser(occurrence.libelleBrut());
        Matiere matiere = cache.computeIfAbsent(libelleNorm,
                norm -> resolveur.resoudre(norm, occurrence.libelleBrut()));
        TypeSeance type = DetecteurTypeSeance.detecter(occurrence.libelleBrut());
        String salleNorm = NormalisateurSalle.normaliser(occurrence.salleBrute());

        Optional<Seance> existante = seances.findByUidExterneAndDebut(
                occurrence.uidExterne(), occurrence.debut());

        if (existante.isEmpty()) {
            Seance seance = new Seance(occurrence.uidExterne(), occurrence.libelleBrut(),
                    occurrence.debut(), occurrence.fin());
            appliquer(seance, occurrence, salleNorm, type, matiere, importAgenda);
            seances.save(seance);
            importAgenda.compterCree();
        } else {
            Seance seance = existante.get();
            if (identique(seance, occurrence, salleNorm, type, matiere)) {
                importAgenda.compterInchange();
            } else {
                appliquer(seance, occurrence, salleNorm, type, matiere, importAgenda);
                importAgenda.compterModifie();
            }
        }
    }

    private void appliquer(Seance seance, OccurrenceBrute occurrence, String salleNorm,
                           TypeSeance type, Matiere matiere, ImportAgenda importAgenda) {
        seance.setFin(occurrence.fin());
        seance.setJourneeEntiere(occurrence.journeeEntiere());
        seance.setSalle(salleNorm);
        seance.setSalleBrute(occurrence.salleBrute());
        seance.setType(type);
        seance.rattacher(matiere);
        seance.rattacherImport(importAgenda);
    }

    private boolean identique(Seance seance, OccurrenceBrute occurrence, String salleNorm,
                              TypeSeance type, Matiere matiere) {
        return !seance.isAnnulee()
                && Objects.equals(seance.getLibelleBrut(), occurrence.libelleBrut())
                && Objects.equals(seance.getFin(), occurrence.fin())
                && seance.isJourneeEntiere() == occurrence.journeeEntiere()
                && Objects.equals(seance.getSalle(), salleNorm)
                && seance.getType() == type
                && Objects.equals(matiereId(seance.getMatiere()), matiereId(matiere));
    }

    private void traiterEcheance(OccurrenceBrute occurrence, ImportAgenda importAgenda,
                                 Map<String, Matiere> cache) {
        String libelleNorm = NormalisateurLibelle.normaliser(occurrence.libelleBrut());
        Matiere matiere = cache.computeIfAbsent(libelleNorm,
                norm -> resolveur.resoudre(norm, occurrence.libelleBrut()));

        Optional<Echeance> existante = echeances.findByUidExterneAndEcheance(
                occurrence.uidExterne(), occurrence.debut());
        if (existante.isPresent()) {
            importAgenda.compterInchange();
            return;
        }
        Echeance echeance = new Echeance(occurrence.uidExterne(), occurrence.libelleBrut(), occurrence.debut());
        echeance.setLibelleBrut(occurrence.libelleBrut());
        echeance.rattacher(matiere);
        echeance.rattacherImport(importAgenda);
        echeances.save(echeance);
        importAgenda.compterCree();
    }

    private void detecterAnnulations(ImportAgenda importAgenda, Set<CleOccurrence> vues) {
        List<Seance> futures = seances.findByDebutAfterAndAnnuleeFalse(Instant.now());
        for (Seance seance : futures) {
            boolean deCetImport = seance.getImportAgenda() != null
                    && importAgenda.getId().equals(seance.getImportAgenda().getId());
            boolean vue = vues.contains(new CleOccurrence(seance.getUidExterne(), seance.getDebut()));
            if (!deCetImport && !vue) {
                seance.marquerAnnulee();
                importAgenda.compterAnnule();
            }
        }
    }

    private static java.util.UUID matiereId(Matiere matiere) {
        return matiere == null ? null : matiere.getId();
    }

    private byte[] lireTout(InputStream flux) {
        try {
            return flux.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du flux impossible", e);
        }
    }

    private String sha256(byte[] contenu) {
        try {
            byte[] empreinte = MessageDigest.getInstance("SHA-256").digest(contenu);
            StringBuilder hex = new StringBuilder(empreinte.length * 2);
            for (byte b : empreinte) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    /** Cle naturelle d'une occurrence, pour la detection des annulations. */
    private record CleOccurrence(String uidExterne, Instant debut) {
    }
}
