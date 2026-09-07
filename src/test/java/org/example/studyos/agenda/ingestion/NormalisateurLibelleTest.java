package org.example.studyos.agenda.ingestion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Toutes les entrees viennent de l'echantillon reel du brief §2 (piege n°3).
 * On verifie que le bruit evident (type, nature, accents, casse, espaces) est retire.
 * On n'attend PAS que toutes les variantes convergent : la table d'alias s'en charge.
 */
class NormalisateurLibelleTest {

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @DisplayName("Suffixe de type retire")
    @CsvSource(delimiter = '|', value = {
            "Qualité SI - CM      | qualite si",
            "Qualité SI -CM       | qualite si",
            "Qualité SI - TD      | qualite si",
            "App Repartie - TD    | app repartie",
            "App Répartie - TD    | app repartie",
            "'App Repartie - TD '  | app repartie",
            "Opti Linéaire - TP   | opti lineaire",
            "Opti Linéaire - Tp   | opti lineaire",
            "Anglais - TD         | anglais",
    })
    void retireSuffixeType(String brut, String attendu) {
        assertEquals(attendu, NormalisateurLibelle.normaliser(brut));
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @DisplayName("Mots de nature retires")
    @CsvSource(delimiter = '|', value = {
            "Examen Qualité des SI     | qualite des si",
            "Soutenance Qualité SI     | qualite si",
            "Examen App Repartie       | app repartie",
            "'Examen  Opti Linéaire'    | opti lineaire",
            "Audit - Examen            | audit",
            "Anglais -                 | anglais",
            "Anglais - Oral Exam       | anglais",
    })
    void retireMotsNature(String brut, String attendu) {
        assertEquals(attendu, NormalisateurLibelle.normaliser(brut));
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @DisplayName("Casse, accents et espaces normalises")
    @CsvSource(delimiter = '|', value = {
            "Audit          | audit",
            "AUDIT          | audit",
            "Process Metier | process metier",
            "Process metier | process metier",
            "Process Métier | process metier",
            "'Anglais '      | anglais",
            "Entrepot des donnée | entrepot des donnee",
    })
    void normaliseCasseAccentsEspaces(String brut, String attendu) {
        assertEquals(attendu, NormalisateurLibelle.normaliser(brut));
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" reste distinct -> \"{1}\"")
    @DisplayName("Variantes NON unifiees par le normaliseur (role de la table d'alias)")
    @CsvSource(delimiter = '|', value = {
            "Processus métier | processus metier",
            "Process Metiers  | process metiers",
            "Urba des SI      | urba des si",
            "Urbanisation des SI | urbanisation des si",
    })
    void neConvergePas(String brut, String attendu) {
        assertEquals(attendu, NormalisateurLibelle.normaliser(brut));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Entree nulle -> chaine vide (pas d'exception)")
    void gereNull() {
        assertEquals("", NormalisateurLibelle.normaliser(null));
    }
}
