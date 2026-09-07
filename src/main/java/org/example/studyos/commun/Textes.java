package org.example.studyos.commun;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilitaires de texte partages par l'ingestion.
 *
 * Distinction importante :
 *  - {@link #simplifier(String)} : normalisation LEGERE (casse, accents, espaces).
 *    Sert a la classification et a la detection de type (brief §5.3, §5.4),
 *    car elle conserve les mots-cles (rendu, examen, tp...).
 *  - La normalisation LOURDE (retrait du type et des mots de nature pour
 *    fabriquer la cle d'alias) vit dans NormalisateurLibelle (brief §5.5).
 */
public final class Textes {

    private Textes() {
    }

    /** NFD puis suppression des diacritiques (é -&gt; e, à -&gt; a...). */
    public static String sansAccents(String texte) {
        if (texte == null) {
            return "";
        }
        String decompose = Normalizer.normalize(texte, Normalizer.Form.NFD);
        return decompose.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /** Minuscules, sans accents, espaces compresses, trim. Conserve les mots. */
    public static String simplifier(String texte) {
        if (texte == null) {
            return "";
        }
        return sansAccents(texte)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
