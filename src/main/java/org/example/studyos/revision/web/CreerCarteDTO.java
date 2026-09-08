package org.example.studyos.revision.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreerCarteDTO(
        @NotNull UUID notionId,
        @NotBlank String question,
        @NotBlank String reponse
) {
}
