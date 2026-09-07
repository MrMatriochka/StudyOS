package org.example.studyos.suivi.web;

import org.example.studyos.agenda.web.MatiereResumeDTO;
import org.example.studyos.suivi.domaine.Echeance;

import java.time.Instant;
import java.util.UUID;

/** Vue d'une echeance pour l'API. */
public record EcheanceDTO(
        UUID id,
        String libelle,
        Instant echeance,
        String etat,
        MatiereResumeDTO matiere
) {

    static EcheanceDTO de(Echeance e) {
        return new EcheanceDTO(
                e.getId(),
                e.getLibelle(),
                e.getEcheance(),
                e.getEtat().name(),
                MatiereResumeDTO.de(e.getMatiere()));
    }
}
