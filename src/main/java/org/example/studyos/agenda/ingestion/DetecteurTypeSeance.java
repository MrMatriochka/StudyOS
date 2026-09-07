package org.example.studyos.agenda.ingestion;

import org.example.studyos.agenda.domaine.TypeSeance;
import org.example.studyos.commun.Textes;

import java.util.regex.Pattern;

/**
 * Detecte le type d'une seance a partir de son libelle (brief §5.4),
 * en normalisation LEGERE. Premier critere qui matche gagne, dans cet ordre :
 *
 *  soutenance                          -&gt; SOUTENANCE
 *  examen | exam | cc | oral           -&gt; EXAMEN
 *  td/tp                               -&gt; TD_TP   (avant td et tp)
 *  tp                                  -&gt; TP
 *  td                                  -&gt; TD
 *  cm                                  -&gt; CM
 *  sinon                               -&gt; INCONNU
 *
 * L'examen l'emporte sur le type de creneau : « Examen App Repartie - TD » est EXAMEN.
 */
public final class DetecteurTypeSeance {

    private static final Pattern SOUTENANCE = Pattern.compile("\\bsoutenance\\b");
    private static final Pattern EXAMEN = Pattern.compile("\\b(examen|exam|cc|oral)\\b");
    private static final Pattern TD_TP = Pattern.compile("\\btd/tp\\b");
    private static final Pattern TP = Pattern.compile("\\btp\\b");
    private static final Pattern TD = Pattern.compile("\\btd\\b");
    private static final Pattern CM = Pattern.compile("\\bcm\\b");

    private DetecteurTypeSeance() {
    }

    public static TypeSeance detecter(String libelleBrut) {
        String s = Textes.simplifier(libelleBrut);

        if (SOUTENANCE.matcher(s).find()) {
            return TypeSeance.SOUTENANCE;
        }
        if (EXAMEN.matcher(s).find()) {
            return TypeSeance.EXAMEN;
        }
        if (TD_TP.matcher(s).find()) {
            return TypeSeance.TD_TP;
        }
        if (TP.matcher(s).find()) {
            return TypeSeance.TP;
        }
        if (TD.matcher(s).find()) {
            return TypeSeance.TD;
        }
        if (CM.matcher(s).find()) {
            return TypeSeance.CM;
        }
        return TypeSeance.INCONNU;
    }
}
