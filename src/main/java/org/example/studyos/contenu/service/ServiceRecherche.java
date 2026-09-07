package org.example.studyos.contenu.service;

import org.example.studyos.contenu.depot.DocumentRepository;
import org.example.studyos.contenu.depot.RechercheProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Recherche plein texte sur les documents (delegue au SQL natif tsvector). */
@Service
public class ServiceRecherche {

    private final DocumentRepository documents;

    public ServiceRecherche(DocumentRepository documents) {
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<RechercheProjection> rechercher(String requete) {
        if (requete == null || requete.isBlank()) {
            return List.of();
        }
        return documents.rechercher(requete.strip());
    }
}
