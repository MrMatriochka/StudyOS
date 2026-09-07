package org.example.studyos.suivi.web;

import jakarta.validation.Valid;
import org.example.studyos.suivi.service.ServiceEcheance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/echeances")
public class ControleurEcheance {

    private final ServiceEcheance serviceEcheance;

    public ControleurEcheance(ServiceEcheance serviceEcheance) {
        this.serviceEcheance = serviceEcheance;
    }

    /** Echeances a venir. Defaut : apres maintenant. Ex : ?apres=2026-03-01T00:00:00Z */
    @GetMapping
    public List<EcheanceDTO> aVenir(@RequestParam(required = false) Instant apres) {
        Instant borne = (apres == null) ? Instant.now() : apres;
        return serviceEcheance.aVenir(borne).stream().map(EcheanceDTO::de).toList();
    }

    @PatchMapping("/{id}")
    public EcheanceDTO changerEtat(@PathVariable UUID id, @Valid @RequestBody MajEtatEcheanceDTO corps) {
        return EcheanceDTO.de(serviceEcheance.changerEtat(id, corps.etat()));
    }
}
