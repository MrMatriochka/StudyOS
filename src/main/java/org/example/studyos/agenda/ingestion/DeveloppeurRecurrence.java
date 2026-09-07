package org.example.studyos.agenda.ingestion;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Developpe les series recurrentes d'un ICalendar en occurrences plates
 * (brief §2 pieges n°1 et n°2, §5.2).
 *
 * Ordre :
 *  1. les VEVENT porteurs d'un RECURRENCE-ID sont mis de cote (overrides),
 *     indexes par (UID, instant du RECURRENCE-ID) ;
 *  2. les VEVENT maitres sont developpes via le DateIterator de biweekly,
 *     qui applique deja RRULE + EXDATE ;
 *  3. pour chaque occurrence developpee, si un override porte sur sa date
 *     d'origine, il l'ecrase (l'heure de debut peut avoir change).
 *
 * Fonction pure vis-a-vis de la base : aucune persistance ici.
 */
public class DeveloppeurRecurrence {

    // Fuseau par defaut pour les dates flottantes ; les DTSTART en TZID/UTC
    // sont resolus par biweekly a partir de la VTIMEZONE / du suffixe Z.
    private static final TimeZone FUSEAU = TimeZone.getTimeZone("Europe/Paris");

    public List<OccurrenceBrute> developper(ICalendar ical) {
        List<VEvent> maitres = new ArrayList<>();
        // uid -> (instant du RECURRENCE-ID -> VEVENT override)
        Map<String, Map<Instant, VEvent>> overrides = new HashMap<>();

        for (VEvent event : ical.getEvents()) {
            Instant recurrenceId = instantRecurrenceId(event);
            if (recurrenceId != null) {
                overrides
                        .computeIfAbsent(uid(event), k -> new HashMap<>())
                        .put(recurrenceId, event);
            } else {
                maitres.add(event);
            }
        }

        List<OccurrenceBrute> occurrences = new ArrayList<>();
        // Marque les overrides reellement appliques, pour ne pas perdre les orphelins.
        Map<String, Map<Instant, VEvent>> restants = copieProfonde(overrides);

        for (VEvent maitre : maitres) {
            String uid = uid(maitre);
            Map<Instant, VEvent> overridesUid = overrides.get(uid);

            DateIterator it = maitre.getDateIterator(FUSEAU);
            while (it.hasNext()) {
                Instant dateOrigine = it.next().toInstant();

                VEvent override = (overridesUid == null) ? null : overridesUid.get(dateOrigine);
                if (override != null) {
                    occurrences.add(versOccurrence(override));
                    restants.get(uid).remove(dateOrigine);
                } else {
                    occurrences.add(versOccurrence(maitre, dateOrigine));
                }
            }
        }

        // Overrides sans maitre correspondant (donnee reelle imparfaite) :
        // on ne les jette pas, on les emet tels quels.
        for (Map<Instant, VEvent> parUid : restants.values()) {
            for (VEvent orphelin : parUid.values()) {
                occurrences.add(versOccurrence(orphelin));
            }
        }

        return occurrences;
    }

    /** Occurrence a la date propre de l'evenement (override ou evenement simple). */
    private OccurrenceBrute versOccurrence(VEvent event) {
        Instant debut = event.getDateStart().getValue().toInstant();
        return versOccurrence(event, debut);
    }

    /** Occurrence a une date de debut imposee (issue du developpement de la RRULE). */
    private OccurrenceBrute versOccurrence(VEvent event, Instant debut) {
        long dureeMs = duree(event);
        Instant fin = debut.plusMillis(dureeMs);
        boolean journeeEntiere = !event.getDateStart().getValue().hasTime();
        String salle = (event.getLocation() == null) ? null : event.getLocation().getValue();
        String libelle = (event.getSummary() == null) ? "" : event.getSummary().getValue();
        return new OccurrenceBrute(uid(event), libelle, debut, fin, salle, journeeEntiere);
    }

    private long duree(VEvent event) {
        if (event.getDateEnd() == null) {
            return 0L;
        }
        long debut = event.getDateStart().getValue().getTime();
        long fin = event.getDateEnd().getValue().getTime();
        return fin - debut;
    }

    private Instant instantRecurrenceId(VEvent event) {
        if (event.getRecurrenceId() == null) {
            return null;
        }
        Date valeur = event.getRecurrenceId().getValue();
        return valeur.toInstant();
    }

    private String uid(VEvent event) {
        return event.getUid().getValue();
    }

    private Map<String, Map<Instant, VEvent>> copieProfonde(Map<String, Map<Instant, VEvent>> source) {
        Map<String, Map<Instant, VEvent>> copie = new HashMap<>();
        source.forEach((uid, parInstant) -> copie.put(uid, new HashMap<>(parInstant)));
        return copie;
    }
}
