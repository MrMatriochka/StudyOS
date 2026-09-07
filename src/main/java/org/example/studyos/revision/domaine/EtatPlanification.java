package org.example.studyos.revision.domaine;

import java.time.LocalDate;

/**
 * Etat de planification d'une carte (SM-2), immuable. Passe en entree et rendu
 * en sortie par le planificateur, sans acces base.
 *
 * @param repetitions       nombre de reussites consecutives
 * @param intervalleJours   intervalle courant en jours
 * @param facilite          facteur de facilite (E-Factor), plancher 1.3
 * @param prochaineEcheance date de la prochaine revision (null a l'etat initial)
 */
public record EtatPlanification(
        int repetitions,
        int intervalleJours,
        double facilite,
        LocalDate prochaineEcheance
) {
    /** Etat d'une carte jamais revisee (defauts du schema V4). */
    public static EtatPlanification initial() {
        return new EtatPlanification(0, 0, 2.5, null);
    }
}
