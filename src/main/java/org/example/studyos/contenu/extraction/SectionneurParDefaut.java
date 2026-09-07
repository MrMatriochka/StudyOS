package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.List;

/** Formats sans structure exploitable (TXT, MD, autre) : une section unique. */
@Component
public class SectionneurParDefaut implements SectionneurDocument {

    @Override
    public List<Section> decouper(String xhtmlTika) {
        String tout = Jsoup.parse(xhtmlTika).text().strip();
        if (tout.isEmpty()) {
            return List.of();
        }
        return List.of(new Section(Granularite.DOCUMENT_ENTIER, null, tout, null, null));
    }
}
