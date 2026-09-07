package org.example.studyos.contenu.web;

import org.example.studyos.contenu.domaine.TypeDocument;

import java.util.UUID;

/**
 * Corps du PATCH /api/documents/{id}. Champs a null = inchanges.
 * detacherSeance=true vide le rattachement (sinon seanceId non-null le fixe).
 */
public record MajDocumentDTO(
        String titre,
        TypeDocument type,
        UUID seanceId,
        boolean detacherSeance
) {
}
