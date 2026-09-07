package org.example.studyos.contenu.extraction;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.stream.Collectors;

/** Helpers de lecture du XHTML Tika (jsoup). */
final class Xhtml {

    private Xhtml() {
    }

    /**
     * Texte d'un conteneur, une ligne par paragraphe (les lignes aident le
     * nettoyage des repetitions ligne a ligne). Si pas de &lt;p&gt;, texte brut.
     */
    static String texte(Element conteneur) {
        Elements paragraphes = conteneur.select("p");
        if (paragraphes.isEmpty()) {
            return conteneur.text().strip();
        }
        return paragraphes.stream()
                .map(Element::text)
                .map(String::strip)
                .filter(ligne -> !ligne.isEmpty())
                .collect(Collectors.joining("\n"));
    }
}
