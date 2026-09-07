package org.example.studyos.agenda.depot;

import org.example.studyos.agenda.domaine.AliasMatiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AliasMatiereRepository extends JpaRepository<AliasMatiere, UUID> {

    /** Resolution de matiere a l'import : cle = libelle normalise. */
    Optional<AliasMatiere> findByLibelleNorm(String libelleNorm);

    /** Fusion : recuperer tous les alias d'une matiere pour les reaffecter. */
    List<AliasMatiere> findByMatiereId(UUID matiereId);
}
