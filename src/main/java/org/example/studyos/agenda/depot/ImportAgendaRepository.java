package org.example.studyos.agenda.depot;

import org.example.studyos.agenda.domaine.ImportAgenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportAgendaRepository extends JpaRepository<ImportAgenda, UUID> {

    /** Idempotence (brief §5.1) : si le hash existe deja, on ne re-importe pas. */
    Optional<ImportAgenda> findByHashFichier(String hashFichier);

    /** Historique des imports, du plus recent au plus ancien. */
    List<ImportAgenda> findAllByOrderByExecuteLeDesc();
}
