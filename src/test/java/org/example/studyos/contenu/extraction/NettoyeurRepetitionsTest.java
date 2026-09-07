package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NettoyeurRepetitionsTest {

    private Section diapo(String texte) {
        return new Section(Granularite.DIAPOSITIVE, null, texte, null, null);
    }

    @Test
    void retireLaLigneRepetee_surPlusDe5Sections() {
        // Pied de page « Cours MIAGE 2026 » present sur les 6 sections.
        List<Section> sections = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            sections.add(diapo("Cours MIAGE 2026\ncontenu propre " + i));
        }

        List<Section> propres = NettoyeurRepetitions.nettoyer(sections);

        for (int i = 0; i < 6; i++) {
            assertFalse(propres.get(i).texte().contains("Cours MIAGE 2026"),
                    "le pied de page recurrent doit disparaitre");
            assertTrue(propres.get(i).texte().contains("contenu propre " + (i + 1)),
                    "le contenu unique doit rester");
        }
    }

    @Test
    void neToucheRienEnDessousDe5Sections() {
        List<Section> sections = List.of(
                diapo("Cours MIAGE\nA"),
                diapo("Cours MIAGE\nB"),
                diapo("Cours MIAGE\nC"));

        List<Section> propres = NettoyeurRepetitions.nettoyer(sections);

        assertEquals(sections, propres, "seuil de 5 sections non atteint : aucun nettoyage");
        assertTrue(propres.get(0).texte().contains("Cours MIAGE"));
    }

    @Test
    void gardeLesLignesLonguesMemeRepetees() {
        String ligneLongue = "Cette phrase depasse largement quatre-vingts caracteres et ne doit donc jamais etre consideree comme un pied de page.";
        List<Section> sections = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            sections.add(diapo(ligneLongue + "\nunique " + i));
        }

        List<Section> propres = NettoyeurRepetitions.nettoyer(sections);

        assertTrue(propres.get(0).texte().contains(ligneLongue),
                "une ligne >= 80 caracteres n'est pas un pied de page");
    }
}
