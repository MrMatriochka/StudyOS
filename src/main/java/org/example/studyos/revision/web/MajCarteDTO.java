package org.example.studyos.revision.web;

import org.example.studyos.revision.domaine.StatutCarte;

public record MajCarteDTO(String question, String reponse, StatutCarte statut) {
}
