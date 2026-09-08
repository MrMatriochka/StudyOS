package org.example.studyos.revision.service;

import org.example.studyos.revision.depot.CarteRepository;
import org.example.studyos.revision.depot.NotionRepository;
import org.example.studyos.revision.domaine.Carte;
import org.example.studyos.revision.domaine.OrigineCarte;
import org.example.studyos.revision.domaine.StatutCarte;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceCarte {

    private final CarteRepository cartes;
    private final NotionRepository notions;

    public ServiceCarte(CarteRepository cartes, NotionRepository notions) {
        this.cartes = cartes;
        this.notions = notions;
    }

    @Transactional(readOnly = true)
    public List<Carte> listerParNotion(UUID notionId) {
        return cartes.findByNotionIdOrderByCreeeLeAsc(notionId);
    }

    @Transactional
    public Carte creer(UUID notionId, String question, String reponse) {
        var notion = notions.findById(notionId)
                .orElseThrow(() -> introuvable("Notion", notionId));
        // Au lot 3, toute carte est saisie a la main.
        return cartes.save(new Carte(notion, question, reponse, OrigineCarte.MANUELLE));
    }

    @Transactional
    public Carte mettreAJour(UUID id, String question, String reponse, StatutCarte statut) {
        Carte carte = charger(id);
        if (question != null && reponse != null) {
            carte.modifier(question, reponse);
        }
        if (statut != null) {
            carte.changerStatut(statut);
        }
        return carte;
    }

    @Transactional
    public void supprimer(UUID id) {
        cartes.delete(charger(id));
    }

    private Carte charger(UUID id) {
        return cartes.findById(id).orElseThrow(() -> introuvable("Carte", id));
    }

    private ResponseStatusException introuvable(String quoi, UUID id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, quoi + " introuvable : " + id);
    }
}
