package org.example.studyos.contenu.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.contenu.depot.RechercheProjection;
import org.example.studyos.contenu.domaine.SectionDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Depot d'un vrai PDF de bout en bout : stockage, extraction, sectionnement,
 * recherche plein texte 'french', et deduplication par hash. Sur vrai Postgres.
 */
@SpringBootTest
@Testcontainers
class ServiceDocumentIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void racineDocuments(DynamicPropertyRegistry registry, @org.junit.jupiter.api.io.TempDir Path dossier) {
        registry.add("studyos.documents.racine", dossier::toString);
    }

    @Autowired
    private ServiceDocument serviceDocument;
    @Autowired
    private ServiceRecherche serviceRecherche;
    @Autowired
    private MatiereRepository matieres;

    private byte[] pdf(String texte) throws Exception {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(texte);
                cs.endText();
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    @Test
    @DisplayName("Depot PDF : extraction + sections + recherche plein texte + dedup")
    void depotEtRecherche() throws Exception {
        UUID matiereId = matieres.save(Matiere.qualifiee("NoSQL")).getId();
        byte[] contenu = pdf("Le theoreme CAP structure les bases de donnees distribuees et la coherence.");

        ResultatDepot depot = serviceDocument.deposer(matiereId, null, "cours-nosql.pdf", "cours (1).pdf", contenu);

        assertFalse(depot.dejaPresent());
        assertTrue(depot.document().isExtractionOk(), () -> "err: " + depot.document().getExtractionErr());
        assertEquals("cours-nosql.pdf", depot.document().getTitre());

        // Sections : une page -> une section.
        List<SectionDocument> sections = serviceDocument.sections(depot.document().getId());
        assertEquals(1, sections.size());
        assertTrue(sections.get(0).getTexte().contains("theoreme"));

        // Recherche 'french' : "distribue" doit matcher "distribuees" (stemming).
        List<RechercheProjection> resultats = serviceRecherche.rechercher("distribue");
        assertEquals(1, resultats.size());
        assertEquals(depot.document().getId(), resultats.get(0).getId());
        assertTrue(resultats.get(0).getExtrait().contains("<b>"), "extrait surligne par ts_headline");

        // Deduplication : meme contenu -> document existant, pas de doublon.
        ResultatDepot rebelote = serviceDocument.deposer(matiereId, null, "autre-nom.pdf", "copie.pdf", contenu);
        assertTrue(rebelote.dejaPresent());
        assertEquals(depot.document().getId(), rebelote.document().getId());
        assertEquals(1, serviceDocument.lister(matiereId, null).size());
    }
}
