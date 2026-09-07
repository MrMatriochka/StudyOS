package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** PDF : une section par page (Tika emet un {@code <div class="page">} par page). */
@Component
public class SectionneurPdf implements SectionneurDocument {

    @Override
    public List<Section> decouper(String xhtmlTika) {
        List<Section> sections = new ArrayList<>();
        int numeroPage = 1;
        for (Element page : Jsoup.parse(xhtmlTika).select("div.page")) {
            String texte = Xhtml.texte(page);
            sections.add(new Section(Granularite.PAGE, null, texte, null, numeroPage));
            numeroPage++;
        }
        return sections;
    }
}
