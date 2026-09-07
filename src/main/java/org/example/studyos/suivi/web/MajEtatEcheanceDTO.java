package org.example.studyos.suivi.web;

import jakarta.validation.constraints.NotNull;
import org.example.studyos.suivi.domaine.EtatEcheance;

/** Corps du PATCH /api/echeances/{id}. */
public record MajEtatEcheanceDTO(@NotNull EtatEcheance etat) {
}
