package org.example.studyos.suivi.depot;

import org.example.studyos.suivi.domaine.Echeance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EcheanceRepository extends JpaRepository<Echeance, UUID> {

    /** Upsert idempotent : cle naturelle (uid_externe, echeance). */
    Optional<Echeance> findByUidExterneAndEcheance(String uidExterne, Instant echeance);

    /** Accueil : echeances a venir apres une date donnee. */
    List<Echeance> findByEcheanceAfterOrderByEcheanceAsc(Instant apres);
}
