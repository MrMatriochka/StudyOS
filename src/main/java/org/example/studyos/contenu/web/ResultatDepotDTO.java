package org.example.studyos.contenu.web;

import org.example.studyos.contenu.service.ResultatDepot;

/** Reponse au depot : le document, et s'il existait deja (dedup par hash). */
public record ResultatDepotDTO(DocumentDTO document, boolean dejaPresent) {

    static ResultatDepotDTO de(ResultatDepot r) {
        return new ResultatDepotDTO(DocumentDTO.de(r.document()), r.dejaPresent());
    }
}
