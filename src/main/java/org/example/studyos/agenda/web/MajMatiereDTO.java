package org.example.studyos.agenda.web;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/** Corps du PUT /api/matieres/{id}. Seul le libelle est obligatoire. */
public record MajMatiereDTO(
        @NotBlank String libelle,
        String code,
        String semestre,
        Integer coefficient,
        String couleur,
        LocalDate dateExamen
) {
}
