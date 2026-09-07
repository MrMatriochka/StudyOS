package org.example.studyos.contenu.web;

import org.example.studyos.contenu.domaine.SectionDocument;

import java.util.UUID;

public record SectionDTO(
        UUID id,
        int ordre,
        String granularite,
        String titre,
        String texte,
        String notes,
        Integer page
) {

    static SectionDTO de(SectionDocument s) {
        return new SectionDTO(s.getId(), s.getOrdre(), s.getGranularite().name(),
                s.getTitre(), s.getTexte(), s.getNotes(), s.getPage());
    }
}
