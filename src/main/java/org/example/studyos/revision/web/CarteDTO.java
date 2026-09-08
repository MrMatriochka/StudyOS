package org.example.studyos.revision.web;

import org.example.studyos.revision.domaine.Carte;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CarteDTO(
        UUID id,
        UUID notionId,
        String question,
        String reponse,
        String origine,
        String statut,
        int repetitions,
        int intervalleJours,
        double facilite,
        LocalDate derniereRevision,
        LocalDate prochaineEcheance,
        Instant creeeLe
) {

    public static CarteDTO de(Carte c) {
        return new CarteDTO(
                c.getId(), c.getNotion().getId(), c.getQuestion(), c.getReponse(),
                c.getOrigine().name(), c.getStatut().name(), c.getRepetitions(),
                c.getIntervalleJours(), c.getFacilite(), c.getDerniereRevision(),
                c.getProchaineEcheance(), c.getCreeeLe());
    }
}
