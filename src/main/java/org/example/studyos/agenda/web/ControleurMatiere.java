package org.example.studyos.agenda.web;

import jakarta.validation.Valid;
import org.example.studyos.agenda.service.ServiceAgenda;
import org.example.studyos.agenda.service.ServiceQualification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matieres")
public class ControleurMatiere {

    private final ServiceQualification qualification;
    private final ServiceAgenda agenda;

    public ControleurMatiere(ServiceQualification qualification, ServiceAgenda agenda) {
        this.qualification = qualification;
        this.agenda = agenda;
    }

    @GetMapping
    public List<MatiereDTO> lister() {
        return qualification.lister().stream().map(MatiereDTO::de).toList();
    }

    @GetMapping("/a-qualifier")
    public List<MatiereDTO> aQualifier() {
        return qualification.listerAQualifier().stream().map(MatiereDTO::de).toList();
    }

    @GetMapping("/{id}")
    public MatiereDTO parId(@PathVariable UUID id) {
        return MatiereDTO.de(qualification.parId(id));
    }

    @GetMapping("/{id}/seances")
    public List<SeanceDTO> seances(@PathVariable UUID id) {
        return agenda.seancesDeMatiere(id).stream().map(SeanceDTO::de).toList();
    }

    @PutMapping("/{id}")
    public MatiereDTO mettreAJour(@PathVariable UUID id, @Valid @RequestBody MajMatiereDTO corps) {
        return MatiereDTO.de(qualification.mettreAJour(id, corps.libelle(), corps.code(),
                corps.semestre(), corps.coefficient(), corps.couleur(), corps.dateExamen()));
    }

    /** Fusion : garde {id}, absorbe {autreId} (alias, seances, echeances reassignes). */
    @PostMapping("/{id}/fusionner/{autreId}")
    public MatiereDTO fusionner(@PathVariable UUID id, @PathVariable UUID autreId) {
        return MatiereDTO.de(qualification.fusionner(id, autreId));
    }

    /** Suppression manuelle : la matiere et ses seances/echeances/alias. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        qualification.supprimer(id);
    }
}
