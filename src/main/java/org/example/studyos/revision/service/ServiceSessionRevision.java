package org.example.studyos.revision.service;

import org.example.studyos.revision.depot.CarteRepository;
import org.example.studyos.revision.depot.RevisionRepository;
import org.example.studyos.revision.domaine.Carte;
import org.example.studyos.revision.domaine.EtatPlanification;
import org.example.studyos.revision.domaine.Revision;
import org.example.studyos.revision.domaine.StatutCarte;
import org.example.studyos.revision.planification.PlanificateurRevision;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Compose la file de revision du jour et enregistre les revisions (brief §lot3).
 *
 * File : cartes dues (echeance &lt;= aujourd'hui) melangees entre matieres, puis
 * cartes nouvelles plafonnees (20/jour par defaut) pour lisser la charge future.
 */
@Service
public class ServiceSessionRevision {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final int PLAFOND_NOUVELLES = 20;

    // Mapping des 4 boutons vers l'echelle SM-2 0-5 (vit ici, pas dans le planificateur).
    private static final int Q_ENCORE = 0;
    private static final int Q_DIFFICILE = 3;
    private static final int Q_BIEN = 4;
    private static final int Q_FACILE = 5;

    private final CarteRepository cartes;
    private final RevisionRepository revisions;
    private final PlanificateurRevision planificateur;

    public ServiceSessionRevision(CarteRepository cartes, RevisionRepository revisions,
                                  PlanificateurRevision planificateur) {
        this.cartes = cartes;
        this.revisions = revisions;
        this.planificateur = planificateur;
    }

    @Transactional(readOnly = true)
    public List<SessionCarte> session(UUID matiereId, Integer limiteNouvelles) {
        LocalDate aujourdhui = LocalDate.now(PARIS);
        int plafond = (limiteNouvelles != null) ? limiteNouvelles : PLAFOND_NOUVELLES;

        List<Carte> dues = (matiereId == null)
                ? cartes.findByStatutAndProchaineEcheanceLessThanEqualOrderByProchaineEcheanceAsc(
                        StatutCarte.VALIDEE, aujourdhui)
                : cartes.findByStatutAndProchaineEcheanceLessThanEqualAndNotion_Matiere_IdOrderByProchaineEcheanceAsc(
                        StatutCarte.VALIDEE, aujourdhui, matiereId);

        List<Carte> nouvelles = (matiereId == null)
                ? cartes.findByStatutAndProchaineEcheanceIsNullOrderByCreeeLeAsc(StatutCarte.VALIDEE)
                : cartes.findByStatutAndProchaineEcheanceIsNullAndNotion_Matiere_IdOrderByCreeeLeAsc(
                        StatutCarte.VALIDEE, matiereId);

        // Melange des dues (l'alternance entre matieres ameliore la retention),
        // puis nouvelles plafonnees et dans l'ordre de creation.
        List<Carte> melangees = new ArrayList<>(dues);
        Collections.shuffle(melangees);

        List<Carte> file = new ArrayList<>(melangees);
        nouvelles.stream().limit(plafond).forEach(file::add);

        return file.stream().map(c -> versSessionCarte(c, aujourdhui)).toList();
    }

    @Transactional
    public Carte enregistrer(UUID carteId, int qualite, Integer dureeMs) {
        Carte carte = cartes.findById(carteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Carte introuvable : " + carteId));
        LocalDate aujourdhui = LocalDate.now(PARIS);

        EtatPlanification nouvel = planificateur.planifier(
                carte.planificationCourante(), qualite, aujourdhui);

        carte.appliquerPlanification(nouvel.repetitions(), nouvel.intervalleJours(),
                nouvel.facilite(), aujourdhui, nouvel.prochaineEcheance());

        revisions.save(new Revision(carte, qualite, nouvel.repetitions(),
                nouvel.intervalleJours(), nouvel.facilite(), dureeMs));
        return carte;
    }

    private SessionCarte versSessionCarte(Carte carte, LocalDate aujourdhui) {
        EtatPlanification etat = carte.planificationCourante();
        return new SessionCarte(carte,
                prevu(etat, Q_ENCORE, aujourdhui),
                prevu(etat, Q_DIFFICILE, aujourdhui),
                prevu(etat, Q_BIEN, aujourdhui),
                prevu(etat, Q_FACILE, aujourdhui));
    }

    private int prevu(EtatPlanification etat, int qualite, LocalDate aujourdhui) {
        return planificateur.planifier(etat, qualite, aujourdhui).intervalleJours();
    }
}
