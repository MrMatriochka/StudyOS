package org.example.studyos.revision.depot;

import org.example.studyos.revision.domaine.Revision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RevisionRepository extends JpaRepository<Revision, UUID> {

    /** Journal d'une carte, pour l'audit et le rejeu. */
    List<Revision> findByCarteIdOrderByFaiteLeAsc(UUID carteId);
}
