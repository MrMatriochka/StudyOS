package org.example.studyos.revision.service;

/**
 * Score de maitrise d'une matiere. evaluee=false (maitrise null) quand la matiere
 * n'a aucune notion : « non evaluee », a ne pas confondre avec 0.
 */
public record ScoreMaitrise(boolean evaluee, Double maitrise, double couverture, double retention) {

    public static ScoreMaitrise nonEvaluee() {
        return new ScoreMaitrise(false, null, 0, 0);
    }
}
