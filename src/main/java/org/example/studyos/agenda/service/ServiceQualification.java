package org.example.studyos.agenda.service;

import org.example.studyos.agenda.depot.AliasMatiereRepository;
import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.agenda.depot.SeanceRepository;
import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.suivi.depot.EcheanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Qualification des matieres (brief §6) : lister, corriger, et surtout fusionner
 * les doublons auto-crees a l'import (« Process Metier » / « Processus metier »).
 */
@Service
public class ServiceQualification {

    private final MatiereRepository matieres;
    private final AliasMatiereRepository alias;
    private final SeanceRepository seances;
    private final EcheanceRepository echeances;

    public ServiceQualification(MatiereRepository matieres, AliasMatiereRepository alias,
                                SeanceRepository seances, EcheanceRepository echeances) {
        this.matieres = matieres;
        this.alias = alias;
        this.seances = seances;
        this.echeances = echeances;
    }

    @Transactional(readOnly = true)
    public List<Matiere> lister() {
        return matieres.findAll();
    }

    @Transactional(readOnly = true)
    public List<Matiere> listerAQualifier() {
        return matieres.findByEnAttenteQualificationTrueOrderByLibelleAsc();
    }

    /** Mise a jour d'une matiere. Editer = valider : la matiere n'est plus « a qualifier ». */
    @Transactional
    public Matiere mettreAJour(UUID id, String libelle, String code, String semestre,
                               Integer coefficient, String couleur, LocalDate dateExamen) {
        Matiere matiere = charger(id);
        matiere.renommer(libelle);
        matiere.setCode(code);
        matiere.setSemestre(semestre);
        matiere.setCoefficient(coefficient);
        matiere.setCouleur(couleur);
        matiere.setDateExamen(dateExamen);
        matiere.marquerQualifiee();
        return matiere;
    }

    /**
     * Fusionne {@code absorbeeId} dans {@code garderId} : reaffecte alias, seances
     * et echeances vers la matiere conservee, puis supprime le doublon.
     */
    @Transactional
    public Matiere fusionner(UUID garderId, UUID absorbeeId) {
        if (garderId.equals(absorbeeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Impossible de fusionner une matiere avec elle-meme");
        }
        Matiere garder = charger(garderId);
        Matiere absorbee = charger(absorbeeId);

        alias.findByMatiereId(absorbee.getId()).forEach(a -> a.reaffecter(garder));
        seances.findByMatiereId(absorbee.getId()).forEach(s -> s.rattacher(garder));
        echeances.findByMatiereId(absorbee.getId()).forEach(e -> e.rattacher(garder));

        matieres.delete(absorbee);
        return garder;
    }

    private Matiere charger(UUID id) {
        return matieres.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Matiere introuvable : " + id));
    }
}
