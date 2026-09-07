package org.example.studyos.agenda.ingestion;

/**
 * Nature d'un evenement de l'agenda (brief §2 piege n°4, §5.3).
 * Le REPERE (semaine d'exam, vacances...) est ignore au lot 1.
 */
public enum Nature {
    SEANCE,
    ECHEANCE,
    REPERE
}
