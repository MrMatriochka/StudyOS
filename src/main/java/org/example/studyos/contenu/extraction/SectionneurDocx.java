package org.example.studyos.contenu.extraction;

import org.example.studyos.contenu.domaine.Granularite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCX / ODT : decoupage sur les titres de niveau 1-2 ({@code <h1>}, {@code <h2>}).
 * Si aucun titre, une seule section (DOCUMENT_ENTIER) : on ne s'acharne pas.
 */
@Component
public class SectionneurDocx implements SectionneurDocument {

    @Override
    public List<Section> decouper(String xhtmlTika) {
        Element corps = Jsoup.parse(xhtmlTika).body();

        List<Section> sections = new ArrayList<>();
        String titreCourant = null;
        StringBuilder texte = new StringBuilder();
        boolean titreVu = false;

        for (Element bloc : corps.children()) {
            if (estTitre(bloc)) {
                titreVu = true;
                ajouter(sections, titreCourant, texte);
                titreCourant = bloc.text().strip();
                texte = new StringBuilder();
            } else {
                String contenu = bloc.text().strip();
                if (!contenu.isEmpty()) {
                    if (texte.length() > 0) {
                        texte.append('\n');
                    }
                    texte.append(contenu);
                }
            }
        }
        ajouter(sections, titreCourant, texte);

        // Aucun titre dans tout le document -> une seule section « document entier ».
        if (!titreVu) {
            String tout = corps.text().strip();
            return tout.isEmpty()
                    ? List.of()
                    : List.of(new Section(Granularite.DOCUMENT_ENTIER, null, tout, null, null));
        }
        return sections;
    }

    private boolean estTitre(Element bloc) {
        String tag = bloc.tagName();
        return tag.equals("h1") || tag.equals("h2");
    }

    private void ajouter(List<Section> sections, String titre, StringBuilder texte) {
        if (titre == null && texte.length() == 0) {
            return; // rien avant le premier titre
        }
        sections.add(new Section(Granularite.TITRE, titre, texte.toString().strip(), null, null));
    }
}
