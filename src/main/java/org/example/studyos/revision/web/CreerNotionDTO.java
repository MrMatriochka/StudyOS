package org.example.studyos.revision.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreerNotionDTO(
        @NotNull UUID matiereId,
        @NotBlank String intitule,
        String description,
        UUID sectionId
) {
}
