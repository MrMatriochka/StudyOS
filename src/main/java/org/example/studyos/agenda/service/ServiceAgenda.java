package org.example.studyos.agenda.service;

import org.example.studyos.agenda.depot.ImportAgendaRepository;
import org.example.studyos.agenda.depot.SeanceRepository;
import org.example.studyos.agenda.domaine.ImportAgenda;
import org.example.studyos.agenda.domaine.Seance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Lectures de l'agenda pour l'API. Les finders utilises chargent la matiere
 * (EntityGraph), donc les entites retournees peuvent etre mappees en DTO
 * sans acces LAZY hors transaction.
 */
@Service
@Transactional(readOnly = true)
public class ServiceAgenda {

    private final SeanceRepository seances;
    private final ImportAgendaRepository imports;

    public ServiceAgenda(SeanceRepository seances, ImportAgendaRepository imports) {
        this.seances = seances;
        this.imports = imports;
    }

    public List<Seance> seancesEntre(Instant du, Instant au) {
        return seances.findByDebutBetweenAndAnnuleeFalseOrderByDebutAsc(du, au);
    }

    public Optional<Seance> prochaineSeance() {
        return seances.findFirstByDebutAfterAndAnnuleeFalseOrderByDebutAsc(Instant.now());
    }

    public List<ImportAgenda> historiqueImports() {
        return imports.findAllByOrderByExecuteLeDesc();
    }
}
