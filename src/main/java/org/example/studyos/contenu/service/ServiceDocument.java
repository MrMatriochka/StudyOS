package org.example.studyos.contenu.service;

import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.agenda.depot.SeanceRepository;
import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.agenda.domaine.Seance;
import org.example.studyos.contenu.depot.DocumentRepository;
import org.example.studyos.contenu.depot.SectionDocumentRepository;
import org.example.studyos.contenu.domaine.Document;
import org.example.studyos.contenu.domaine.SectionDocument;
import org.example.studyos.contenu.domaine.TypeDocument;
import org.example.studyos.contenu.extraction.ExtracteurTexte;
import org.example.studyos.contenu.extraction.FabriqueSectionneur;
import org.example.studyos.contenu.extraction.NettoyeurRepetitions;
import org.example.studyos.contenu.extraction.ResultatExtraction;
import org.example.studyos.contenu.extraction.Section;
import org.example.studyos.contenu.stockage.DepotFichier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Depot et cycle de vie des documents (brief §lot2). Extraction synchrone :
 * sur un usage local avec des fichiers de quelques Mo, l'asynchrone n'apporterait
 * que de la complexite d'etat pour economiser deux secondes.
 */
@Service
public class ServiceDocument {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final int FENETRE_JOURS = 10;

    private final DocumentRepository documents;
    private final SectionDocumentRepository sections;
    private final MatiereRepository matieres;
    private final SeanceRepository seances;
    private final DepotFichier depot;
    private final ExtracteurTexte extracteur;
    private final FabriqueSectionneur fabrique;

    public ServiceDocument(DocumentRepository documents, SectionDocumentRepository sections,
                           MatiereRepository matieres, SeanceRepository seances,
                           DepotFichier depot, ExtracteurTexte extracteur, FabriqueSectionneur fabrique) {
        this.documents = documents;
        this.sections = sections;
        this.matieres = matieres;
        this.seances = seances;
        this.depot = depot;
        this.extracteur = extracteur;
        this.fabrique = fabrique;
    }

    @Transactional
    public ResultatDepot deposer(UUID matiereId, UUID seanceId, String titreDemande,
                                 String nomOriginal, byte[] contenu) {
        Matiere matiere = matieres.findById(matiereId)
                .orElseThrow(() -> introuvable("Matiere", matiereId));

        String hash = sha256(contenu);
        Optional<Document> existant = documents.findByHash(hash);
        if (existant.isPresent()) {
            return new ResultatDepot(existant.get(), true);
        }

        String extension = extension(nomOriginal);
        String titre = (titreDemande != null && !titreDemande.isBlank())
                ? titreDemande.strip() : sansExtension(nomOriginal);

        Document document = new Document(matiere, titre, nomOriginal,
                typeParDefaut(extension), extension, hash, contenu.length);
        if (seanceId != null) {
            document.rattacherSeance(seances.findById(seanceId)
                    .orElseThrow(() -> introuvable("Seance", seanceId)));
        }

        String chemin = depot.stocker(contenu, hash, document.getId(), extension);
        document.definirChemin(chemin);

        ResultatExtraction extrait = extracteur.extraire(depot.chemin(chemin));
        document.renseignerExtraction(extrait.ok(), extrait.texte(), extrait.nbPages(), extrait.erreur());
        documents.save(document);

        if (extrait.ok()) {
            enregistrerSections(document, extrait);
        }
        return new ResultatDepot(document, false);
    }

    private void enregistrerSections(Document document, ResultatExtraction extrait) {
        List<Section> decoupe = NettoyeurRepetitions.nettoyer(
                fabrique.pour(extrait.mimeType()).decouper(extrait.xhtml()));
        int ordre = 0;
        for (Section section : decoupe) {
            if (section.texte() == null || section.texte().isBlank()) {
                continue; // une diapo/page vide ne fait pas une section
            }
            sections.save(new SectionDocument(document, ordre++, section.granularite(),
                    section.titre(), section.texte(), section.notes(), section.page()));
        }
    }

    @Transactional(readOnly = true)
    public Document parId(UUID id) {
        return documents.findWithMatiereById(id).orElseThrow(() -> introuvable("Document", id));
    }

    @Transactional(readOnly = true)
    public List<Document> lister(UUID matiereId, UUID seanceId) {
        return (seanceId == null)
                ? documents.findByMatiereIdOrderByAjouteLeDesc(matiereId)
                : documents.findByMatiereIdAndSeanceIdOrderByAjouteLeDesc(matiereId, seanceId);
    }

    @Transactional(readOnly = true)
    public List<SectionDocument> sections(UUID documentId) {
        return sections.findByDocumentIdOrderByOrdreAsc(documentId);
    }

    /** Octets du fichier sur disque, pour le servir en flux binaire. */
    public byte[] octets(Document document) {
        return depot.lire(document.getChemin());
    }

    @Transactional
    public Document mettreAJour(UUID id, String titre, TypeDocument type,
                                UUID seanceId, boolean detacherSeance) {
        Document document = documents.findWithMatiereById(id).orElseThrow(() -> introuvable("Document", id));
        if (titre != null && !titre.isBlank()) {
            document.renommer(titre.strip());
        }
        if (type != null) {
            document.changerType(type);
        }
        if (detacherSeance) {
            document.rattacherSeance(null);
        } else if (seanceId != null) {
            document.rattacherSeance(seances.findById(seanceId)
                    .orElseThrow(() -> introuvable("Seance", seanceId)));
        }
        return document;
    }

    @Transactional
    public void supprimer(UUID id) {
        Document document = documents.findById(id).orElseThrow(() -> introuvable("Document", id));
        String chemin = document.getChemin();
        documents.delete(document); // sections en cascade (FK on delete cascade)
        depot.supprimer(chemin);    // puis le fichier sur disque
    }

    /** Propose la seance de la matiere la plus proche d'aujourd'hui (fenetre +/-10 j). */
    @Transactional(readOnly = true)
    public Optional<Seance> proposerSeance(UUID matiereId, LocalDate aujourdhui) {
        return seances.findByMatiereId(matiereId).stream()
                .filter(s -> Math.abs(ecartJours(s.getDebut(), aujourdhui)) <= FENETRE_JOURS)
                .min(Comparator.comparingLong(s -> Math.abs(ecartJours(s.getDebut(), aujourdhui))));
    }

    private long ecartJours(Instant debut, LocalDate aujourdhui) {
        LocalDate jourSeance = debut.atZone(PARIS).toLocalDate();
        return Duration.between(aujourdhui.atStartOfDay(), jourSeance.atStartOfDay()).toDays();
    }

    private TypeDocument typeParDefaut(String extension) {
        return switch (extension) {
            case "pptx", "ppt" -> TypeDocument.SLIDES;
            default -> TypeDocument.AUTRE;
        };
    }

    private String extension(String nomOriginal) {
        if (nomOriginal == null) {
            return "bin";
        }
        int point = nomOriginal.lastIndexOf('.');
        if (point < 0 || point == nomOriginal.length() - 1) {
            return "bin";
        }
        return nomOriginal.substring(point + 1).toLowerCase();
    }

    private String sansExtension(String nomOriginal) {
        if (nomOriginal == null) {
            return "Document";
        }
        int point = nomOriginal.lastIndexOf('.');
        return (point > 0) ? nomOriginal.substring(0, point) : nomOriginal;
    }

    private ResponseStatusException introuvable(String quoi, UUID id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, quoi + " introuvable : " + id);
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
}
