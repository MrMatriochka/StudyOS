package org.example.studyos.revision.service;

import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.contenu.depot.SectionDocumentRepository;
import org.example.studyos.revision.depot.CarteRepository;
import org.example.studyos.revision.depot.NotionRepository;
import org.example.studyos.revision.domaine.Notion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceNotion {

    private final NotionRepository notions;
    private final MatiereRepository matieres;
    private final SectionDocumentRepository sections;
    private final CarteRepository cartes;

    public ServiceNotion(NotionRepository notions, MatiereRepository matieres,
                         SectionDocumentRepository sections, CarteRepository cartes) {
        this.notions = notions;
        this.matieres = matieres;
        this.sections = sections;
        this.cartes = cartes;
    }

    /** Notion + nombre de cartes (pour le marqueur « trou de revision »). */
    public record NotionAvecCompte(Notion notion, long nbCartes) {
    }

    @Transactional(readOnly = true)
    public List<NotionAvecCompte> lister(UUID matiereId) {
        return notions.findByMatiereIdOrderByIntituleAsc(matiereId).stream()
                .map(n -> new NotionAvecCompte(n, cartes.countByNotionId(n.getId())))
                .toList();
    }

    @Transactional
    public NotionAvecCompte creer(UUID matiereId, String intitule, String description, UUID sectionId) {
        var matiere = matieres.findById(matiereId)
                .orElseThrow(() -> introuvable("Matiere", matiereId));
        Notion notion = new Notion(matiere, intitule);
        if (description != null) {
            notion.decrire(description);
        }
        if (sectionId != null) {
            notion.rattacherSection(sections.findById(sectionId)
                    .orElseThrow(() -> introuvable("Section", sectionId)));
        }
        return new NotionAvecCompte(notions.save(notion), 0);
    }

    @Transactional
    public NotionAvecCompte mettreAJour(UUID id, String intitule, String description) {
        Notion notion = charger(id);
        if (intitule != null && !intitule.isBlank()) {
            notion.renommer(intitule.strip());
        }
        if (description != null) {
            notion.decrire(description);
        }
        return new NotionAvecCompte(notion, cartes.countByNotionId(id));
    }

    @Transactional
    public void supprimer(UUID id) {
        notions.delete(charger(id)); // cartes/revisions en cascade (FK)
    }

    private Notion charger(UUID id) {
        return notions.findById(id).orElseThrow(() -> introuvable("Notion", id));
    }

    private ResponseStatusException introuvable(String quoi, UUID id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, quoi + " introuvable : " + id);
    }
}
