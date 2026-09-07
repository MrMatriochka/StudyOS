package org.example.studyos.agenda.ingestion;

/**
 * Bilan d'un import (brief §5). Retourne au controleur puis au front.
 *
 * @param dejaImporte true si le fichier avait deja ete importe (meme hash) : rien n'a ete fait
 */
public record ResultatImport(
        boolean dejaImporte,
        int crees,
        int modifies,
        int inchanges,
        int annules
) {
    /** Fabrique : fichier deja importe (meme hash), aucun changement. */
    public static ResultatImport inchange() {
        return new ResultatImport(true, 0, 0, 0, 0);
    }
}
