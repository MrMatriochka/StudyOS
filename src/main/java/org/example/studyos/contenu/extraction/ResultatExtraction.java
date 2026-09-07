package org.example.studyos.contenu.extraction;

/**
 * Resultat d'une extraction Tika. L'echec est une donnee (brief §lot2), pas une
 * exception : un document non extractible reste ajoutable et ouvrable.
 *
 * @param texte    texte brut (pour texte_extrait / tsvector)
 * @param xhtml    XHTML structure de Tika (pour le decoupage en sections)
 * @param nbPages  nombre de pages/diapositives si connu, sinon null
 * @param mimeType type MIME detecte (pour choisir le sectionneur)
 */
public record ResultatExtraction(
        boolean ok,
        String texte,
        String xhtml,
        Integer nbPages,
        String mimeType,
        String erreur
) {

    public static ResultatExtraction reussi(String texte, String xhtml, Integer nbPages, String mimeType) {
        return new ResultatExtraction(true, texte, xhtml, nbPages, mimeType, null);
    }

    public static ResultatExtraction echec(String erreur) {
        return new ResultatExtraction(false, null, null, null, null, erreur);
    }
}
