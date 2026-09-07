package org.example.studyos.agenda.ingestion;

import biweekly.Biweekly;
import biweekly.ICalendar;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Implementation ICS de {@link SourceAgenda} : parse le flux avec biweekly
 * puis developpe la recurrence. Ne persiste rien.
 */
@Component
public class SourceIcs implements SourceAgenda {

    private final DeveloppeurRecurrence developpeur;

    public SourceIcs(DeveloppeurRecurrence developpeur) {
        this.developpeur = developpeur;
    }

    @Override
    public List<OccurrenceBrute> lire(InputStream flux) {
        try {
            ICalendar ical = Biweekly.parse(flux).first();
            if (ical == null) {
                return List.of();
            }
            return developpeur.developper(ical);
        } catch (IOException e) {
            throw new UncheckedIOException("Fichier ICS illisible", e);
        }
    }
}
