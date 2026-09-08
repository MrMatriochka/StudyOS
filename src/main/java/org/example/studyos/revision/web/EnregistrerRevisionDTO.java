package org.example.studyos.revision.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** qualite sur l'echelle SM-2 0-5 (le front mappe ses 4 boutons vers ces valeurs). */
public record EnregistrerRevisionDTO(
        @NotNull UUID carteId,
        @Min(0) @Max(5) int qualite,
        Integer dureeMs
) {
}
