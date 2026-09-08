package org.example.studyos.revision.web;

import jakarta.validation.Valid;
import org.example.studyos.revision.service.ServiceCarte;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ControleurCarte {

    private final ServiceCarte service;

    public ControleurCarte(ServiceCarte service) {
        this.service = service;
    }

    @GetMapping("/api/notions/{notionId}/cartes")
    public List<CarteDTO> parNotion(@PathVariable UUID notionId) {
        return service.listerParNotion(notionId).stream().map(CarteDTO::de).toList();
    }

    @PostMapping("/api/cartes")
    public CarteDTO creer(@Valid @RequestBody CreerCarteDTO corps) {
        return CarteDTO.de(service.creer(corps.notionId(), corps.question(), corps.reponse()));
    }

    @PatchMapping("/api/cartes/{id}")
    public CarteDTO mettreAJour(@PathVariable UUID id, @RequestBody MajCarteDTO corps) {
        return CarteDTO.de(service.mettreAJour(id, corps.question(), corps.reponse(), corps.statut()));
    }

    @DeleteMapping("/api/cartes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
