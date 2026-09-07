package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SectionneurPdfTest {

    private final SectionneurPdf sectionneur = new SectionneurPdf();

    @Test
    void uneSectionParPageNumerotee() {
        String xhtml = """
                <html><body>
                  <div class="page"><p>contenu page un</p></div>
                  <div class="page"><p>contenu page deux</p></div>
                  <div class="page"><p>contenu page trois</p></div>
                </body></html>
                """;

        List<Section> sections = sectionneur.decouper(xhtml);

        assertEquals(3, sections.size());
        assertEquals(Granularite.PAGE, sections.get(0).granularite());
        assertEquals(1, sections.get(0).page());
        assertEquals("contenu page deux", sections.get(1).texte());
        assertEquals(3, sections.get(2).page());
    }
}
