package org.example.studyos.contenu.web;

import org.example.studyos.contenu.service.ServiceRecherche;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recherche")
public class ControleurRecherche {

    private final ServiceRecherche service;

    public ControleurRecherche(ServiceRecherche service) {
        this.service = service;
    }

    @GetMapping
    public List<ResultatRechercheDTO> rechercher(@RequestParam String q) {
        return service.rechercher(q).stream().map(ResultatRechercheDTO::de).toList();
    }
}
