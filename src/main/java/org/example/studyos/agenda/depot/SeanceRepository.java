package org.example.studyos.agenda.depot;

import org.example.studyos.agenda.domaine.Seance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeanceRepository extends JpaRepository<Seance, UUID> {

    /** Upsert idempotent (brief §5.8) : cle naturelle (uid_externe, debut). */
    Optional<Seance> findByUidExterneAndDebut(String uidExterne, Instant debut);

    /**
     * Ecran Semaine : seances sur une plage [du, au].
     * EntityGraph : charge la matiere dans la meme requete pour permettre le
     * mapping DTO hors transaction (LAZY + open-in-view=false sinon).
     */
    @EntityGraph(attributePaths = "matiere")
    List<Seance> findByDebutBetweenAndAnnuleeFalseOrderByDebutAsc(Instant du, Instant au);

    /** Carte « prochain cours » : premiere seance a venir, non annulee. */
    @EntityGraph(attributePaths = "matiere")
    Optional<Seance> findFirstByDebutAfterAndAnnuleeFalseOrderByDebutAsc(Instant maintenant);

    /** Detection des annulations (brief §5.9) : seances futures non annulees. */
    List<Seance> findByDebutAfterAndAnnuleeFalse(Instant maintenant);

    /** Fusion de matieres : reassigner les seances de la matiere absorbee. */
    List<Seance> findByMatiereId(UUID matiereId);
}
