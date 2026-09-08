package org.example.studyos.revision.depot;

import org.example.studyos.revision.domaine.Notion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotionRepository extends JpaRepository<Notion, UUID> {

    List<Notion> findByMatiereIdOrderByIntituleAsc(UUID matiereId);

    /** Denominateur du score de maitrise (couverture). */
    long countByMatiereId(UUID matiereId);
}
