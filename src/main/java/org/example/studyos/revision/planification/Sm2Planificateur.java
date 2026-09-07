package org.example.studyos.revision.planification;

import org.example.studyos.revision.domaine.EtatPlanification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Implementation SM-2 (brief §lot3). Fonction pure.
 *
 * Regles :
 *  1. echec (qualite &lt; 3)  -> repetitions = 0, intervalle = 1 jour ;
 *  2. sinon selon repetitions : 0 -&gt; 1 j, 1 -&gt; 6 j, sinon round(intervalle x facilite),
 *     puis repetitions++ ;
 *  3. dans tous les cas : facilite += 0.1 - (5-q)(0.08 + (5-q)0.02), plancher 1.3 ;
 *  4. prochaine echeance = aujourd'hui + intervalle.
 */
@Component
public class Sm2Planificateur implements PlanificateurRevision {

    private static final double FACILITE_PLANCHER = 1.3;

    @Override
    public EtatPlanification planifier(EtatPlanification courant, int qualite, LocalDate aujourdhui) {
        int repetitions;
        int intervalle;

        if (qualite < 3) {
            repetitions = 0;
            intervalle = 1;
        } else {
            intervalle = switch (courant.repetitions()) {
                case 0 -> 1;
                case 1 -> 6;
                default -> (int) Math.round(courant.intervalleJours() * courant.facilite());
            };
            repetitions = courant.repetitions() + 1;
        }

        double facilite = ajusterFacilite(courant.facilite(), qualite);
        LocalDate prochaine = aujourdhui.plusDays(intervalle);
        return new EtatPlanification(repetitions, intervalle, facilite, prochaine);
    }

    private double ajusterFacilite(double facilite, int qualite) {
        int ecart = 5 - qualite;
        double nouvelle = facilite + (0.1 - ecart * (0.08 + ecart * 0.02));
        return Math.max(FACILITE_PLANCHER, nouvelle);
    }
}
