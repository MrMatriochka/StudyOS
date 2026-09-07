package org.example.studyos.revision.planification;

import org.example.studyos.revision.domaine.EtatPlanification;

import java.time.LocalDate;

/**
 * Calcule le prochain etat de planification d'une carte apres une revision.
 * Fonction pure : la date est un parametre (jamais LocalDate.now() a l'interieur).
 * Interface car une 2e implementation est identifiee (FSRS).
 */
public interface PlanificateurRevision {

    EtatPlanification planifier(EtatPlanification courant, int qualite, LocalDate aujourdhui);
}
