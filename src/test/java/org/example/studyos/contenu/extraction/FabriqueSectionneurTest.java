package org.example.studyos.contenu.extraction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FabriqueSectionneurTest {

    private final FabriqueSectionneur fabrique = new FabriqueSectionneur(
            new SectionneurPdf(), new SectionneurPptx(), new SectionneurDocx(), new SectionneurParDefaut());

    @Test
    void choisitLeSectionneurSelonLeMime() {
        assertInstanceOf(SectionneurPdf.class, fabrique.pour("application/pdf"));
        assertInstanceOf(SectionneurPptx.class,
                fabrique.pour("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        assertInstanceOf(SectionneurDocx.class,
                fabrique.pour("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertInstanceOf(SectionneurParDefaut.class, fabrique.pour("text/plain"));
        assertInstanceOf(SectionneurParDefaut.class, fabrique.pour(null));
    }
}
