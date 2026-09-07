package org.example.studyos.agenda.ingestion;

import java.time.Instant;

/**
 * Une occurrence de VEVENT apres developpement de la recurrence, avant toute
 * classification/normalisation. On conserve les valeurs brutes (libelle, salle) :
 * les etapes suivantes de l'import s'en servent sans re-parser l'ICS.
 *
 * @param uidExterne     UID du VEVENT (partage entre maitre et overrides)
 * @param libelleBrut    SUMMARY tel quel
 * @param debut          instant de debut (deja resolu depuis TZID/UTC)
 * @param fin            instant de fin
 * @param salleBrute     LOCATION tel quel, ou null si absent
 * @param journeeEntiere true si l'evenement est en VALUE=DATE (journee entiere)
 */
public record OccurrenceBrute(
        String uidExterne,
        String libelleBrut,
        Instant debut,
        Instant fin,
        String salleBrute,
        boolean journeeEntiere
) {
}
