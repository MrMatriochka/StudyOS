package org.example.studyos.contenu.extraction;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.example.studyos.contenu.domaine.Granularite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Rectangle;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Valide la chaine ExtracteurTika + FabriqueSectionneur sur de VRAIS fichiers
 * generes a la volee (PDFBox/POI, deja au classpath via Tika). Confirme que les
 * classes CSS du XHTML Tika (div.page, div.slide-content) sont les bonnes.
 */
class ExtractionReelleTest {

    private final ExtracteurTika extracteur = new ExtracteurTika();
    private final FabriqueSectionneur fabrique = new FabriqueSectionneur(
            new SectionneurPdf(), new SectionneurPptx(), new SectionneurDocx(), new SectionneurParDefaut());

    @Test
    void pdfDeuxPages_donneDeuxSectionsPage(@TempDir Path dossier) throws Exception {
        Path pdf = dossier.resolve("cours.pdf");
        try (PDDocument doc = new PDDocument()) {
            ecrirePage(doc, "Page une : introduction au cours et objectifs pedagogiques.");
            ecrirePage(doc, "Page deux : contenu detaille et exemples pratiques.");
            doc.save(pdf.toFile());
        }

        ResultatExtraction extrait = extracteur.extraire(pdf);
        assertTrue(extrait.ok(), () -> "extraction PDF: " + extrait.erreur());
        assertEquals(2, extrait.nbPages());

        List<Section> sections = fabrique.pour(extrait.mimeType()).decouper(extrait.xhtml());
        assertEquals(2, sections.size());
        assertEquals(Granularite.PAGE, sections.get(0).granularite());
        assertTrue(sections.get(0).texte().contains("introduction"));
    }

    @Test
    void pdfSansTexte_estDetecteCommeScanne(@TempDir Path dossier) throws Exception {
        Path pdf = dossier.resolve("scanne.pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage()); // pages vides, aucune couche texte
            doc.addPage(new PDPage());
            doc.save(pdf.toFile());
        }

        ResultatExtraction extrait = extracteur.extraire(pdf);

        assertFalse(extrait.ok());
        assertTrue(extrait.erreur().contains("scanne"));
    }

    @Test
    void pptxDeuxDiapos_donneDeuxSectionsDiapositive(@TempDir Path dossier) throws Exception {
        Path pptx = dossier.resolve("slides.pptx");
        try (XMLSlideShow ppt = new XMLSlideShow(); OutputStream out = Files.newOutputStream(pptx)) {
            ajouterDiapo(ppt, "Diapositive un : definitions.");
            ajouterDiapo(ppt, "Diapositive deux : applications.");
            ppt.write(out);
        }

        ResultatExtraction extrait = extracteur.extraire(pptx);
        assertTrue(extrait.ok(), () -> "extraction PPTX: " + extrait.erreur());

        List<Section> sections = fabrique.pour(extrait.mimeType()).decouper(extrait.xhtml());
        assertEquals(2, sections.size(), "une section par diapositive");
        assertEquals(Granularite.DIAPOSITIVE, sections.get(0).granularite());
        assertTrue(sections.get(0).texte().contains("definitions"));
    }

    private void ecrirePage(PDDocument doc, String texte) throws Exception {
        PDPage page = new PDPage();
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(50, 700);
            cs.showText(texte);
            cs.endText();
        }
    }

    private void ajouterDiapo(XMLSlideShow ppt, String texte) {
        XSLFSlide diapo = ppt.createSlide();
        XSLFTextBox zone = diapo.createTextBox();
        zone.setAnchor(new Rectangle(50, 50, 500, 200));
        zone.setText(texte);
    }
}
