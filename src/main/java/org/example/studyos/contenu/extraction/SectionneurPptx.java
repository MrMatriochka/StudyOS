package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * PPTX : une section par diapositive ({@code <div class="slide-content">}).
 * Les notes du presentateur ({@code <div class="slide-notes">}) sont appariees
 * par index et stockees a part (jamais concatenees au texte de la diapo).
 */
@Component
public class SectionneurPptx implements SectionneurDocument {

    @Override
    public List<Section> decouper(String xhtmlTika) {
        Document doc = Jsoup.parse(xhtmlTika);
        Elements diapos = doc.select("div.slide-content");
        Elements notes = doc.select("div.slide-notes");

        List<Section> sections = new ArrayList<>();
        for (int i = 0; i < diapos.size(); i++) {
            String texte = Xhtml.texte(diapos.get(i));
            String note = (i < notes.size()) ? vide(Xhtml.texte(notes.get(i))) : null;
            sections.add(new Section(Granularite.DIAPOSITIVE, null, texte, note, i + 1));
        }
        return sections;
    }

    private String vide(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
