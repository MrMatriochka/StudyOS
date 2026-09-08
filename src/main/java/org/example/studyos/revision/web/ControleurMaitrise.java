package org.example.studyos.revision.web;

import org.example.studyos.revision.service.ScoreMaitrise;
import org.example.studyos.revision.service.ServiceMaitrise;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ControleurMaitrise {

    private final ServiceMaitrise service;

    public ControleurMaitrise(ServiceMaitrise service) {
        this.service = service;
    }

    @GetMapping("/api/matieres/{id}/maitrise")
    public ScoreMaitrise maitrise(@PathVariable UUID id) {
        return service.maitrise(id);
    }
}
