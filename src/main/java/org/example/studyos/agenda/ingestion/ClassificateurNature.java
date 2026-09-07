package org.example.studyos.agenda.ingestion;

import org.example.studyos.commun.Textes;

import java.util.regex.Pattern;

/**
 * Route un evenement vers sa nature (brief §5.3), sur le libelle en
 * normalisation LEGERE (les mots-cles sont conserves).
 *
 * Regles, dans l'ordre :
 *  1. contient rendu / remise / livrable / depot           -&gt; ECHEANCE
 *  2. journee entiere ET semaine / vacances / jnm          -&gt; REPERE
 *  3. sinon                                                 -&gt; SEANCE
 */
public final class ClassificateurNature {

    private static final Pattern ECHEANCE = Pattern.compile("\\b(rendu|remise|livrable|depot)");
    private static final Pattern REPERE = Pattern.compile("\\b(semaine|vacances|jnm)\\b");

    private ClassificateurNature() {
    }

    public static Nature classer(String libelleBrut, boolean journeeEntiere) {
        String s = Textes.simplifier(libelleBrut);

        if (ECHEANCE.matcher(s).find()) {
            return Nature.ECHEANCE;
        }
        if (journeeEntiere && REPERE.matcher(s).find()) {
            return Nature.REPERE;
        }
        return Nature.SEANCE;
    }
}
