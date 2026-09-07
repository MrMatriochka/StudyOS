package org.example.studyos.agenda.service;

import org.example.studyos.agenda.depot.AliasMatiereRepository;
import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.agenda.depot.SeanceRepository;
import org.example.studyos.agenda.domaine.Matiere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fusion de matieres sur vrai Postgres : le doublon disparait, ses seances,
 * echeances et alias basculent sur la matiere conservee.
 */
@SpringBootTest
@Testcontainers
class ServiceQualificationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ServiceImport serviceImport;
    @Autowired
    private ServiceQualification qualification;
    @Autowired
    private MatiereRepository matieres;
    @Autowired
    private SeanceRepository seances;
    @Autowired
    private AliasMatiereRepository alias;

    @BeforeEach
    void importer() {
        serviceImport.importer("extrait.ics", getClass().getResourceAsStream("/agenda/extrait-recurrence.ics"));
    }

    @Test
    @DisplayName("Fusion : IA absorbee dans Anglais, 5 seances basculent, doublon supprime")
    void fusion() {
        // A l'import : 2 matieres a qualifier, triees -> [Anglais, IA]
        List<Matiere> aQualifier = matieres.findByEnAttenteQualificationTrueOrderByLibelleAsc();
        assertEquals(2, aQualifier.size());
        Matiere anglais = aQualifier.get(0);
        Matiere ia = aQualifier.get(1);
        assertEquals("Anglais", anglais.getLibelle());
        assertEquals("IA", ia.getLibelle());

        qualification.fusionner(anglais.getId(), ia.getId());

        // Le doublon a disparu, il ne reste qu'une matiere.
        assertEquals(1, matieres.count());
        assertTrue(matieres.findById(ia.getId()).isEmpty());

        // Les 5 seances (1 Anglais + 4 IA) pointent desormais sur Anglais.
        assertEquals(5, seances.findByMatiereId(anglais.getId()).size());
        assertEquals(0, seances.findByMatiereId(ia.getId()).size());

        // Les alias des deux matieres sont regroupes sur Anglais.
        assertEquals(2, alias.findByMatiereId(anglais.getId()).size());
    }

    @Test
    @DisplayName("Fusion d'une matiere avec elle-meme : refusee")
    void fusionSurSoiMemeRefusee() {
        Matiere une = matieres.findAll().get(0);
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> qualification.fusionner(une.getId(), une.getId()));
    }
}
