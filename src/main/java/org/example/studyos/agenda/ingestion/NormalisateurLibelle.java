package org.example.studyos.agenda.ingestion;

import org.example.studyos.commun.Textes;

import java.util.Locale;

/**
 * Normalise un libelle de matiere brut en une cle stable (brief §5.5),
 * utilisee ensuite comme cle de resolution dans alias_matiere.
 *
 * Fonction pure, sans dependance : memes entrees -&gt; memes sorties.
 * La normalisation est volontairement imparfaite : elle ne cherche pas a
 * unifier toutes les variantes (« qualite si » vs « qualite des si »), c'est
 * le role de la table d'alias semi-manuelle. Ici on nettoie le bruit evident.
 */
public final class NormalisateurLibelle {

    // Suffixe de type en fin de chaine : « - CM », « -CM », « - TD/TP », « - Tp »...
    private static final String SUFFIXE_TYPE = "\\s*-\\s*(td/tp|cm|td|tp)\\s*$";

    // Mots de nature, n'importe ou dans le libelle.
    private static final String MOTS_NATURE = "\\b(examen|exam|soutenance|oral|cc|rendu|dossier)\\b";

    private NormalisateurLibelle() {
    }

    public static String normaliser(String brut) {
        if (brut == null) {
            return "";
        }
        String s = Textes.sansAccents(brut).toLowerCase(Locale.ROOT);
        s = s.replaceAll(SUFFIXE_TYPE, " ");   // 1. retirer le suffixe de type
        s = s.replaceAll(MOTS_NATURE, " ");    // 2. retirer les mots de nature
        s = s.replace('-', ' ');               // 3. tirets residuels -> espace
        s = s.replaceAll("\\s+", " ").trim();  //    compresser les espaces, trim
        return s;
    }
}
