package org.example.studyos.agenda.web;

import org.example.studyos.agenda.service.ServiceAgenda;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/seances")
public class ControleurAgenda {

    private final ServiceAgenda serviceAgenda;

    public ControleurAgenda(ServiceAgenda serviceAgenda) {
        this.serviceAgenda = serviceAgenda;
    }

    /**
     * Seances sur une plage. Dates en ISO-8601 instant, ex :
     * /api/seances?du=2026-03-01T00:00:00Z&au=2026-03-31T23:59:59Z
     */
    @GetMapping
    public List<SeanceDTO> surPlage(@RequestParam Instant du, @RequestParam Instant au) {
        return serviceAgenda.seancesEntre(du, au).stream().map(SeanceDTO::de).toList();
    }

    /** Prochaine seance a venir (204 si aucune). */
    @GetMapping("/prochaine")
    public ResponseEntity<SeanceDTO> prochaine() {
        return serviceAgenda.prochaineSeance()
                .map(SeanceDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
