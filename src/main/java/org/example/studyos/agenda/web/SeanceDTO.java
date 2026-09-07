package org.example.studyos.agenda.web;

import org.example.studyos.agenda.domaine.Seance;

import java.time.Instant;
import java.util.UUID;

/**
 * Vue d'une seance pour l'API. Les instants sont exposes tels quels (UTC) ;
 * la conversion vers Europe/Paris se fait a l'affichage cote front.
 */
public record SeanceDTO(
        UUID id,
        String libelleBrut,
        Instant debut,
        Instant fin,
        boolean journeeEntiere,
        String salle,
        String type,
        boolean annulee,
        MatiereResumeDTO matiere
) {

    public static SeanceDTO de(Seance seance) {
        return new SeanceDTO(
                seance.getId(),
                seance.getLibelleBrut(),
                seance.getDebut(),
                seance.getFin(),
                seance.isJourneeEntiere(),
                seance.getSalle(),
                seance.getType() == null ? null : seance.getType().name(),
                seance.isAnnulee(),
                MatiereResumeDTO.de(seance.getMatiere()));
    }
}
