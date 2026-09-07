package org.example.studyos.contenu.depot;

import java.util.UUID;

/** Projection d'un resultat de recherche plein texte (alias colonnes = getters). */
public interface RechercheProjection {

    UUID getId();

    String getTitre();

    /** Extrait surligne par ts_headline (balises <b>...</b>). */
    String getExtrait();

    double getRang();
}
