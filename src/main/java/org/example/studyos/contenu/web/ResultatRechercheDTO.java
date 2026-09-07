package org.example.studyos.contenu.web;

import org.example.studyos.contenu.depot.RechercheProjection;

import java.util.UUID;

/** Un resultat de recherche : le document et un extrait surligne. */
public record ResultatRechercheDTO(UUID documentId, String titre, String extrait, double rang) {

    static ResultatRechercheDTO de(RechercheProjection p) {
        return new ResultatRechercheDTO(p.getId(), p.getTitre(), p.getExtrait(), p.getRang());
    }
}
