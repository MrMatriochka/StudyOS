package org.example.studyos.contenu.depot;

import org.example.studyos.contenu.domaine.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @EntityGraph(attributePaths = {"matiere", "seance"})
    Optional<Document> findWithMatiereById(UUID id);

    /**
     * Recherche plein texte (brief §lot2) : SQL natif car tsvector/ts_rank/ts_headline
     * sont specifiques a Postgres. ts_rank ordonne, ts_headline surligne les extraits.
     */
    @Query(value = """
            select d.id as id,
                   d.titre as titre,
                   ts_headline('french', coalesce(d.texte_extrait, ''),
                               plainto_tsquery('french', :q),
                               'MaxFragments=2, MinWords=5, MaxWords=18') as extrait,
                   ts_rank(d.recherche, plainto_tsquery('french', :q)) as rang
            from document d
            where d.recherche @@ plainto_tsquery('french', :q)
            order by rang desc
            limit 30
            """, nativeQuery = true)
    List<RechercheProjection> rechercher(@Param("q") String q);
}
