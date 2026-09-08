package org.example.studyos.revision.service;

import org.example.studyos.revision.depot.CarteRepository;
import org.example.studyos.revision.depot.NotionRepository;
import org.example.studyos.revision.domaine.Carte;
import org.example.studyos.revision.domaine.StatutCarte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Score de maitrise = couverture x retention, calcule a la demande (jamais stocke).
 *
 * couverture = notions ayant au moins une carte validee / total des notions.
 * retention  = moyenne sur les cartes validees de exp(-jours / max(intervalle, 1)),
 *              approximation de la courbe d'oubli d'Ebbinghaus. Consequence voulue :
 *              le score decroit tout seul si on ne revise pas.
 */
@Service
public class ServiceMaitrise {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    private final NotionRepository notions;
    private final CarteRepository cartes;

    public ServiceMaitrise(NotionRepository notions, CarteRepository cartes) {
        this.notions = notions;
        this.cartes = cartes;
    }

    @Transactional(readOnly = true)
    public ScoreMaitrise maitrise(UUID matiereId) {
        long total = notions.countByMatiereId(matiereId);
        if (total == 0) {
            return ScoreMaitrise.nonEvaluee(); // aucune notion : non evaluee, pas 0
        }

        long couvertes = cartes.compterNotionsCouvertes(StatutCarte.VALIDEE, matiereId);
        double couverture = (double) couvertes / total;

        List<Carte> validees = cartes.findByStatutAndNotion_Matiere_Id(StatutCarte.VALIDEE, matiereId);
        double retention = validees.isEmpty() ? 0.0
                : validees.stream().mapToDouble(this::retention).average().orElse(0.0);

        return new ScoreMaitrise(true, couverture * retention, couverture, retention);
    }

    private double retention(Carte carte) {
        LocalDate derniere = carte.getDerniereRevision();
        if (derniere == null) {
            return 0.0; // jamais revisee : retention non demontree
        }
        long jours = Math.max(0, ChronoUnit.DAYS.between(derniere, LocalDate.now(PARIS)));
        int intervalle = Math.max(1, carte.getIntervalleJours());
        return Math.exp(-(double) jours / intervalle);
    }
}
