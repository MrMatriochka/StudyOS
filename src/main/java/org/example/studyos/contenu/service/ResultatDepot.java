package org.example.studyos.contenu.service;

import org.example.studyos.contenu.domaine.Document;

/**
 * Resultat d'un depot. dejaPresent = le fichier existait deja (meme hash) :
 * on renvoie l'existant sans recreer de doublon (brief §lot2).
 */
public record ResultatDepot(Document document, boolean dejaPresent) {
}
