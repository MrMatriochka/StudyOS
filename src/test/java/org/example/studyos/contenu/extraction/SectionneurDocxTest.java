package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SectionneurDocxTest {

    private final SectionneurDocx sectionneur = new SectionneurDocx();

    @Test
    void decoupeSurLesTitresH1H2() {
        String xhtml = """
                <html><body>
                  <h1>Introduction</h1>
                  <p>texte d'intro</p>
                  <h2>Partie A</h2>
                  <p>ligne 1</p>
                  <p>ligne 2</p>
                </body></html>
                """;

        List<Section> sections = sectionneur.decouper(xhtml);

        assertEquals(2, sections.size());
        assertEquals(Granularite.TITRE, sections.get(0).granularite());
        assertEquals("Introduction", sections.get(0).titre());
        assertEquals("texte d'intro", sections.get(0).texte());
        assertEquals("Partie A", sections.get(1).titre());
        assertEquals("ligne 1\nligne 2", sections.get(1).texte());
    }

    @Test
    void sansTitreUneSeuleSectionDocumentEntier() {
        String xhtml = "<html><body><p>juste du texte</p><p>sans titre</p></body></html>";

        List<Section> sections = sectionneur.decouper(xhtml);

        assertEquals(1, sections.size());
        assertEquals(Granularite.DOCUMENT_ENTIER, sections.get(0).granularite());
    }
}
