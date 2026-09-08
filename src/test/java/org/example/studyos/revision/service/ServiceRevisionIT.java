package org.example.studyos.revision.service;

import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.revision.domaine.Carte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class ServiceRevisionIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired ServiceNotion serviceNotion;
    @Autowired ServiceCarte serviceCarte;
    @Autowired ServiceSessionRevision serviceSession;
    @Autowired ServiceMaitrise serviceMaitrise;
    @Autowired MatiereRepository matieres;

    private UUID nouvelleMatiere(String libelle) {
        return matieres.save(Matiere.qualifiee(libelle)).getId();
    }

    private UUID nouvelleNotion(UUID matiereId, String intitule) {
        return serviceNotion.creer(matiereId, intitule, null, null).notion().getId();
    }

    @Test
    @DisplayName("Session : 100 nouvelles cartes -> plafonnees a 20")
    void plafondNouvellesCartes() {
        UUID matiereId = nouvelleMatiere("Algo");
        UUID notionId = nouvelleNotion(matiereId, "Tri");
        for (int i = 0; i < 100; i++) {
            serviceCarte.creer(notionId, "Q" + i, "R" + i);
        }

        var session = serviceSession.session(matiereId, null);

        assertEquals(20, session.size(), "plafond de 20 nouvelles cartes par jour");
    }

    @Test
    @DisplayName("Maitrise : matiere sans notion -> non evaluee (null, pas 0)")
    void maitriseNonEvaluee() {
        UUID matiereId = nouvelleMatiere("Vide");

        ScoreMaitrise score = serviceMaitrise.maitrise(matiereId);

        assertFalse(score.evaluee());
        assertNull(score.maitrise());
    }

    @Test
    @DisplayName("Maitrise : cartes revisees aujourd'hui -> proche de la couverture")
    void maitriseProcheCouverture() {
        UUID matiereId = nouvelleMatiere("BD");
        UUID n1 = nouvelleNotion(matiereId, "Normalisation");
        UUID n2 = nouvelleNotion(matiereId, "Transactions");
        nouvelleNotion(matiereId, "Index"); // notion sans carte -> trou

        Carte c1 = serviceCarte.creer(n1, "1NF ?", "...");
        Carte c2 = serviceCarte.creer(n2, "ACID ?", "...");
        serviceSession.enregistrer(c1.getId(), 4, null); // revisees aujourd'hui
        serviceSession.enregistrer(c2.getId(), 4, null);

        ScoreMaitrise score = serviceMaitrise.maitrise(matiereId);

        assertTrue(score.evaluee());
        assertEquals(2.0 / 3.0, score.couverture(), 1e-9, "2 notions couvertes sur 3");
        assertEquals(1.0, score.retention(), 1e-9, "revisees aujourd'hui -> retention 1");
        assertEquals(2.0 / 3.0, score.maitrise(), 1e-9);
    }
}
