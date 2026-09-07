package org.example.studyos.contenu.depot;

import org.example.studyos.contenu.domaine.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /** Deduplication par empreinte de contenu (hash unique). */
    Optional<Document> findByHash(String hash);

    /** Documents d'une matiere (matiere chargee pour le DTO). */
    @EntityGraph(attributePaths = {"matiere", "seance"})
    List<Document> findByMatiereIdOrderByAjouteLeDesc(UUID matiereId);

    @EntityGraph(attributePaths = {"matiere", "seance"})
    List<Document> findByMatiereIdAndSeanceIdOrderByAjouteLeDesc(UUID matiereId, UUID seanceId);
}
