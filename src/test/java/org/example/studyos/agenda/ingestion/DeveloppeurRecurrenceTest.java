package org.example.studyos.agenda.ingestion;

import biweekly.Biweekly;
import biweekly.ICalendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cas obligatoires du brief §8, sur un extrait reel du .ics de la promo
 * (src/test/resources/agenda/extrait-recurrence.ics).
 */
class DeveloppeurRecurrenceTest {

    private final DeveloppeurRecurrence developpeur = new DeveloppeurRecurrence();
    private List<OccurrenceBrute> occurrences;

    @BeforeEach
    void developperExtrait() throws Exception {
        try (InputStream flux = getClass().getResourceAsStream("/agenda/extrait-recurrence.ics")) {
            assertNotNull(flux, "fixture ICS introuvable");
            ICalendar ical = Biweekly.parse(flux).first();
            occurrences = developpeur.developper(ical);
        }
    }

    @Test
    @DisplayName("Anglais : COUNT=2 + 1 EXDATE -> une seule seance (le 15/06)")
    void anglaisUneSeuleSeance() {
        List<OccurrenceBrute> anglais = occurrences.stream()
                .filter(o -> o.libelleBrut().equals("Anglais"))
                .toList();

        assertEquals(1, anglais.size(), "l'EXDATE du 22/06 doit retirer la 2e occurrence");
        // 15/06/2026 13:30 Europe/Paris (heure d'ete, +02:00) = 11:30 UTC
        assertEquals(Instant.parse("2026-06-15T11:30:00Z"), anglais.get(0).debut());
    }

    @Test
    @DisplayName("IA : serie COUNT=4, override du 13/03 deplace 13h30 -> 15h45")
    void iaOverrideDeplaceLHeure() {
        List<OccurrenceBrute> ia = occurrences.stream()
                .filter(o -> o.libelleBrut().equals("IA"))
                .sorted((a, b) -> a.debut().compareTo(b.debut()))
                .toList();

        assertEquals(4, ia.size(), "4 vendredis: 06, 13, 20, 27 mars");

        // Le 13/03 n'existe PAS a 13h30 (heure d'hiver +01:00 -> 12:30 UTC)...
        boolean existeA1330 = ia.stream()
                .anyMatch(o -> o.debut().equals(Instant.parse("2026-03-13T12:30:00Z")));
        assertTrue(!existeA1330, "l'occurrence 13h30 du 13/03 doit avoir ete ecrasee");

        // ...mais bien a 15h45 (14:45 UTC), avec la salle de l'override.
        OccurrenceBrute le13mars = ia.stream()
                .filter(o -> o.debut().equals(Instant.parse("2026-03-13T14:45:00Z")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("occurrence du 13/03 15h45 absente"));
        assertEquals("U2-114", le13mars.salleBrute());
    }

    @Test
    @DisplayName("Total : 1 Anglais + 4 IA = 5 occurrences developpees")
    void totalOccurrences() {
        assertEquals(5, occurrences.size());
    }
}
