package org.example.studyos.agenda.ingestion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Variantes de salles reelles du brief §2 / regle §5.7. */
class NormalisateurSalleTest {

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @DisplayName("Casse et espaces autour du tiret normalises")
    @CsvSource(delimiter = '|', value = {
            "U4-100   | U4-100",
            "u4-100   | U4-100",
            "'U4 - 100' | U4-100",
            "1TP1-B08 | 1TP1-B08",
            "'1TP1 - B08' | 1TP1-B08",
    })
    void normaliseCasseEtTiret(String brut, String attendu) {
        assertEquals(attendu, NormalisateurSalle.normaliser(brut));
    }

    @Test
    @DisplayName("O entoure de chiffres corrige en zero (coquille U4-3O1)")
    void corrigeOEnZero() {
        assertEquals("U4-301", NormalisateurSalle.normaliser("U4-3O1"));
    }

    @Test
    @DisplayName("Compromis assume : variante separee par une espace non unifiee au tiret")
    void varianteEspaceNonUnifiee() {
        assertEquals("1TP1 B08", NormalisateurSalle.normaliser("1TP1 B08"));
    }

    @Test
    @DisplayName("Salle absente ou vide -> null")
    void gereVideEtNull() {
        assertNull(NormalisateurSalle.normaliser(null));
        assertNull(NormalisateurSalle.normaliser("   "));
    }
}
