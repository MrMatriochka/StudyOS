package org.example.studyos.agenda.web;

import org.example.studyos.agenda.domaine.ImportAgenda;

import java.time.Instant;
import java.util.UUID;

/** Vue d'un import pour l'historique. */
public record ImportDTO(
        UUID id,
        String source,
        String nomFichier,
        Instant executeLe,
        int crees,
        int modifies,
        int inchanges,
        int annules
) {

    static ImportDTO de(ImportAgenda i) {
        return new ImportDTO(
                i.getId(), i.getSource(), i.getNomFichier(), i.getExecuteLe(),
                i.getNbCrees(), i.getNbModifies(), i.getNbInchanges(), i.getNbAnnules());
    }
}
