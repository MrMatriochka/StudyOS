package org.example.studyos.revision.web;

import org.example.studyos.revision.service.ServiceNotion.NotionAvecCompte;

import java.time.Instant;
import java.util.UUID;

public record NotionDTO(
        UUID id,
        UUID matiereId,
        String intitule,
        String description,
        Instant creeeLe,
        long nbCartes
) {

    static NotionDTO de(NotionAvecCompte n) {
        return new NotionDTO(
                n.notion().getId(),
                n.notion().getMatiere().getId(),
                n.notion().getIntitule(),
                n.notion().getDescription(),
                n.notion().getCreeeLe(),
                n.nbCartes());
    }
}
