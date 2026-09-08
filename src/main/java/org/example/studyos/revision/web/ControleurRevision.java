package org.example.studyos.revision.web;

import jakarta.validation.Valid;
import org.example.studyos.revision.service.ServiceSessionRevision;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ControleurRevision {

    private final ServiceSessionRevision service;

    public ControleurRevision(ServiceSessionRevision service) {
        this.service = service;
    }

    @GetMapping("/api/revisions/session")
    public List<SessionCarteDTO> session(@RequestParam(required = false) UUID matiereId,
                                         @RequestParam(required = false) Integer limite) {
        return service.session(matiereId, limite).stream().map(SessionCarteDTO::de).toList();
    }

    /** Enregistre une revision et retourne la carte avec son nouvel etat. */
    @PostMapping("/api/revisions")
    public CarteDTO enregistrer(@Valid @RequestBody EnregistrerRevisionDTO corps) {
        return CarteDTO.de(service.enregistrer(corps.carteId(), corps.qualite(), corps.dureeMs()));
    }
}
