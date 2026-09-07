package org.example.studyos.suivi.depot;

import org.example.studyos.suivi.domaine.Echeance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EcheanceRepository extends JpaRepository<Echeance, UUID> {

    /** Upsert idempotent : cle naturelle (uid_externe, echeance). */
    Optional<Echeance> findByUidExterneAndEcheance(String uidExterne, Instant echeance);

    /** Accueil : echeances a venir apres une date donnee (matiere chargee pour le DTO). */
    @EntityGraph(attributePaths = "matiere")
    List<Echeance> findByEcheanceAfterOrderByEcheanceAsc(Instant apres);

    /** Echeance avec sa matiere chargee (mapping DTO hors transaction). */
    @EntityGraph(attributePaths = "matiere")
    Optional<Echeance> findWithMatiereById(UUID id);

    /** Fusion de matieres : reassigner les echeances de la matiere absorbee. */
    List<Echeance> findByMatiereId(UUID matiereId);
}
