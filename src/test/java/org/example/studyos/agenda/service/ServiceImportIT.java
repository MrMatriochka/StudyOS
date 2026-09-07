package org.example.studyos.agenda.service;

import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.agenda.depot.SeanceRepository;
import org.example.studyos.agenda.domaine.Seance;
import org.example.studyos.agenda.ingestion.ResultatImport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cas obligatoires du brief §8, de bout en bout sur un vrai Postgres 16
 * (Testcontainers). Necessite un daemon Docker sur la machine qui execute.
 */
@SpringBootTest
@Testcontainers
class ServiceImportIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    private static final String UID_IA = "20rvus0gvrvk8rtef6gqm76tod@google.com";

    @Autowired
    private ServiceImport serviceImport;
    @Autowired
    private SeanceRepository seances;
    @Autowired
    private MatiereRepository matieres;

    private InputStream extrait() {
        return getClass().getResourceAsStream("/agenda/extrait-recurrence.ics");
    }

    @Test
    @DisplayName("Import complet : 5 seances, override IA a 15h45, 2 matieres a qualifier, idempotence")
    void importComplet() {
        // --- 1er import ---
        ResultatImport premier = serviceImport.importer("extrait-recurrence.ics", extrait());

        assertFalse(premier.dejaImporte());
        assertEquals(5, premier.crees(), "1 Anglais (COUNT=2 - EXDATE) + 4 IA (COUNT=4)");
        assertEquals(5, seances.count());

        // L'override du 13/03 : la seance existe a 15h45 (14:45 UTC), pas a 13h30, salle U2-114.
        Seance le13mars = seances.findByUidExterneAndDebut(UID_IA, Instant.parse("2026-03-13T14:45:00Z"))
                .orElseThrow(() -> new AssertionError("seance IA du 13/03 15h45 absente"));
        assertEquals("U2-114", le13mars.getSalle());
        assertTrue(seances.findByUidExterneAndDebut(UID_IA, Instant.parse("2026-03-13T12:30:00Z")).isEmpty(),
                "l'occurrence 13h30 ne doit pas exister");

        // IA et Anglais auto-crees en attente de qualification.
        assertEquals(2, matieres.findByEnAttenteQualificationTrueOrderByLibelleAsc().size());

        // --- 2e import du meme fichier : idempotence (brief §8) ---
        ResultatImport second = serviceImport.importer("extrait-recurrence.ics", extrait());

        assertTrue(second.dejaImporte());
        assertEquals(5, seances.count(), "aucune seance supplementaire");
    }
}
