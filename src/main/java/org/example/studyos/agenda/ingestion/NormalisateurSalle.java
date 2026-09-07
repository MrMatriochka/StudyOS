package org.example.studyos.agenda.ingestion;

import java.util.Locale;

/**
 * Normalise un identifiant de salle brut (brief §5.7).
 * Fonction pure. Le libelle_brut d'origine est conserve par ailleurs
 * dans l'entite, on ne perd jamais la donnee source.
 */
public final class NormalisateurSalle {

    private NormalisateurSalle() {
    }

    /**
     * @return la salle normalisee, ou {@code null} si l'entree est vide/absente
     *         (LOCATION manque sur ~1 evenement sur 5).
     */
    public static String normaliser(String brut) {
        if (brut == null) {
            return null;
        }
        String s = brut.trim();
        if (s.isEmpty()) {
            return null;
        }
        s = s.toUpperCase(Locale.ROOT);
        s = s.replaceAll("\\s*-\\s*", "-");             // espaces autour du tiret
        s = s.replaceAll("\\s+", " ").trim();           // espaces multiples restants
        s = s.replaceAll("(?<=[0-9])O(?=[0-9])", "0");  // O entre chiffres = zero (U4-3O1)
        return s;
    }
}
