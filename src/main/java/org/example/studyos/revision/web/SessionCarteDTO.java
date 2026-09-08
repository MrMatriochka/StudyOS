package org.example.studyos.revision.web;

import org.example.studyos.revision.service.SessionCarte;

import java.util.UUID;

/** Carte a reviser + intervalle prevu (jours) pour chaque bouton de notation. */
public record SessionCarteDTO(
        UUID id,
        String question,
        String reponse,
        int prevuEncore,
        int prevuDifficile,
        int prevuBien,
        int prevuFacile
) {

    static SessionCarteDTO de(SessionCarte s) {
        return new SessionCarteDTO(
                s.carte().getId(), s.carte().getQuestion(), s.carte().getReponse(),
                s.prevuEncore(), s.prevuDifficile(), s.prevuBien(), s.prevuFacile());
    }
}
