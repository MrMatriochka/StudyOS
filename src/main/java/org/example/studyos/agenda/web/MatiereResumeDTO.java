package org.example.studyos.agenda.web;

import org.example.studyos.agenda.domaine.Matiere;

import java.util.UUID;

/** Vue compacte d'une matiere pour l'embarquer dans une seance. */
public record MatiereResumeDTO(UUID id, String libelle, String couleur, boolean aQualifier) {

    public static MatiereResumeDTO de(Matiere matiere) {
        if (matiere == null) {
            return null;
        }
        return new MatiereResumeDTO(
                matiere.getId(),
                matiere.getLibelle(),
                matiere.getCouleur(),
                matiere.isEnAttenteQualification());
    }
}
