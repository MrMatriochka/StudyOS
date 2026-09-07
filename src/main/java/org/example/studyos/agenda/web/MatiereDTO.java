package org.example.studyos.agenda.web;

import org.example.studyos.agenda.domaine.Matiere;

import java.time.LocalDate;
import java.util.UUID;

/** Vue complete d'une matiere. */
public record MatiereDTO(
        UUID id,
        String code,
        String libelle,
        String semestre,
        Integer coefficient,
        String couleur,
        LocalDate dateExamen,
        boolean aQualifier
) {

    static MatiereDTO de(Matiere m) {
        return new MatiereDTO(
                m.getId(), m.getCode(), m.getLibelle(), m.getSemestre(),
                m.getCoefficient(), m.getCouleur(), m.getDateExamen(),
                m.isEnAttenteQualification());
    }
}
