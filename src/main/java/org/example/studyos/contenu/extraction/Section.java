package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;

/**
 * Section extraite avant persistance (l'ordre est attribue a l'insertion).
 * `notes` (notes du presentateur) reste distincte de `texte`, jamais concatenee.
 */
public record Section(
        Granularite granularite,
        String titre,
        String texte,
        String notes,
        Integer page
) {
    /** Copie avec un nouveau texte (utilise par le nettoyage des repetitions). */
    public Section avecTexte(String nouveauTexte) {
        return new Section(granularite, titre, nouveauTexte, notes, page);
    }
}
