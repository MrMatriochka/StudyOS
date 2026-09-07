package org.example.studyos.contenu.extraction;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extraction via Apache Tika, en une seule passe (TeeContentHandler) :
 *  - ToXMLContentHandler -> XHTML structure (pages, diapos, titres) pour le sectionnement,
 *  - BodyContentHandler  -> texte brut (sans limite de taille) pour l'index plein texte.
 *
 * Garde-fou PDF scanne : moins de 50 caracteres/page en moyenne -> extraction echouee,
 * pour ne pas fabriquer de sections vides que le lot 3 exploiterait a tort.
 */
@Component
public class ExtracteurTika implements ExtracteurTexte {

    private static final int SEUIL_CAR_PAR_PAGE = 50;

    @Override
    public ResultatExtraction extraire(Path fichier) {
        AutoDetectParser parser = new AutoDetectParser();
        ToXMLContentHandler xml = new ToXMLContentHandler();
        BodyContentHandler texte = new BodyContentHandler(-1); // -1 = pas de limite
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fichier.getFileName().toString());

        try (InputStream flux = Files.newInputStream(fichier)) {
            parser.parse(flux, new TeeContentHandler(xml, texte), metadata, new ParseContext());
        } catch (Exception e) {
            return ResultatExtraction.echec("extraction impossible : " + e.getMessage());
        }

        String contenu = texte.toString();
        Integer nbPages = lireNbPages(metadata);
        String mime = sansParametres(metadata.get(Metadata.CONTENT_TYPE));

        if (pdfProbablementScanne(contenu, nbPages)) {
            return ResultatExtraction.echec("pas de couche texte, PDF probablement scanne");
        }
        return ResultatExtraction.reussi(contenu, xml.toString(), nbPages, mime);
    }

    private boolean pdfProbablementScanne(String texte, Integer nbPages) {
        if (nbPages == null || nbPages <= 0) {
            return false;
        }
        return texte.strip().length() / nbPages < SEUIL_CAR_PAR_PAGE;
    }

    private Integer lireNbPages(Metadata metadata) {
        for (String cle : new String[] {"xmpTPg:NPages", "Page-Count", "slide-count", "meta:page-count"}) {
            String valeur = metadata.get(cle);
            if (valeur != null) {
                try {
                    return Integer.valueOf(valeur.trim());
                } catch (NumberFormatException ignore) {
                    // cle presente mais non numerique : on essaie la suivante
                }
            }
        }
        return null;
    }

    private String sansParametres(String contentType) {
        if (contentType == null) {
            return null;
        }
        int point = contentType.indexOf(';');
        return (point >= 0 ? contentType.substring(0, point) : contentType).trim();
    }
}
