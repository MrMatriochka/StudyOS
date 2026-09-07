package org.example.studyos.agenda.ingestion;

import java.io.InputStream;
import java.util.List;

/**
 * Source d'occurrences d'agenda. Abstraction : demain une autre source
 * (flux ADE, API...) s'ajoutera sans toucher au service d'import (brief §5).
 */
public interface SourceAgenda {

    List<OccurrenceBrute> lire(InputStream flux);
}
