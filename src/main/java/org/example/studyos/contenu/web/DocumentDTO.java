package org.example.studyos.contenu.web;

import org.example.studyos.agenda.web.MatiereResumeDTO;
import org.example.studyos.contenu.domaine.Document;

import java.time.Instant;
import java.util.UUID;

public record DocumentDTO(
        UUID id,
        String titre,
        String nomOriginal,
        String type,
        String extension,
        long tailleOctets,
        Integer nbPages,
        boolean extractionOk,
        String extractionErr,
        Instant ajouteLe,
        MatiereResumeDTO matiere,
        UUID seanceId,
        Instant seanceDebut
) {

    public static DocumentDTO de(Document d) {
        return new DocumentDTO(
                d.getId(), d.getTitre(), d.getNomOriginal(), d.getType().name(), d.getExtension(),
                d.getTailleOctets(), d.getNbPages(), d.isExtractionOk(), d.getExtractionErr(),
                d.getAjouteLe(), MatiereResumeDTO.de(d.getMatiere()),
                d.getSeance() == null ? null : d.getSeance().getId(),
                d.getSeance() == null ? null : d.getSeance().getDebut());
    }
}
