package org.example.studyos.contenu.depot;

import org.example.studyos.contenu.domaine.SectionDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SectionDocumentRepository extends JpaRepository<SectionDocument, UUID> {

    List<SectionDocument> findByDocumentIdOrderByOrdreAsc(UUID documentId);
}
