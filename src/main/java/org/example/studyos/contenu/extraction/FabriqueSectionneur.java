package org.example.studyos.contenu.extraction;

import org.springframework.stereotype.Component;

/** Choisit le sectionneur selon le type MIME (meme motif que SourceAgenda). */
@Component
public class FabriqueSectionneur {

    private static final String PDF = "application/pdf";
    private static final String PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String ODT = "application/vnd.oasis.opendocument.text";

    private final SectionneurPdf pdf;
    private final SectionneurPptx pptx;
    private final SectionneurDocx docx;
    private final SectionneurParDefaut parDefaut;

    public FabriqueSectionneur(SectionneurPdf pdf, SectionneurPptx pptx,
                               SectionneurDocx docx, SectionneurParDefaut parDefaut) {
        this.pdf = pdf;
        this.pptx = pptx;
        this.docx = docx;
        this.parDefaut = parDefaut;
    }

    public SectionneurDocument pour(String mimeType) {
        if (mimeType == null) {
            return parDefaut;
        }
        return switch (mimeType) {
            case PDF -> pdf;
            case PPTX -> pptx;
            case DOCX, ODT -> docx;
            default -> parDefaut;
        };
    }
}
