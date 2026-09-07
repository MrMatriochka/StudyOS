package org.example.studyos.contenu.domaine;

/**
 * Unite structurelle d'une section, selon le format d'origine (brief lot 2).
 * Le lot 3 en a besoin : une diapositive de 40 mots ne se traite pas comme
 * un document entier de 20 pages.
 */
public enum Granularite {
    DIAPOSITIVE,
    PAGE,
    TITRE,
    DOCUMENT_ENTIER
}
