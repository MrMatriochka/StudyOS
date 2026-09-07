package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SectionneurPptxTest {

    private final SectionneurPptx sectionneur = new SectionneurPptx();

    @Test
    void uneSectionParDiapoAvecNotesApparieees() {
        String xhtml = """
                <html><body>
                  <div class="slide-content"><p>Diapo 1</p><p>point cle</p></div>
                  <div class="slide-notes"><p>ce que dit le prof</p></div>
                  <div class="slide-content"><p>Diapo 2</p></div>
                </body></html>
                """;

        List<Section> sections = sectionneur.decouper(xhtml);

        assertEquals(2, sections.size());
        assertEquals(Granularite.DIAPOSITIVE, sections.get(0).granularite());
        assertEquals("Diapo 1\npoint cle", sections.get(0).texte());
        assertEquals("ce que dit le prof", sections.get(0).notes());
        assertEquals(1, sections.get(0).page());

        assertEquals("Diapo 2", sections.get(1).texte());
        assertNull(sections.get(1).notes(), "diapo sans notes -> notes null, jamais concatenees");
        assertEquals(2, sections.get(1).page());
    }
}
