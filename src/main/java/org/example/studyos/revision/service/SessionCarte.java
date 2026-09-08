package org.example.studyos.revision.service;

import org.example.studyos.revision.domaine.Carte;

/**
 * Une carte de la file de revision, avec l'intervalle prevu (en jours) pour
 * chacun des 4 boutons : c'est ce qui permet a l'utilisateur de noter juste.
 */
public record SessionCarte(
        Carte carte,
        int prevuEncore,
        int prevuDifficile,
        int prevuBien,
        int prevuFacile
) {
}
