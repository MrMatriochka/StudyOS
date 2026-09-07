package org.example.studyos.contenu.extraction;

import java.nio.file.Path;

/**
 * Extrait le texte d'un fichier. Interface pour isoler la dependance Tika
 * derriere une frontiere qu'on controle (meme motif que SourceAgenda au lot 1).
 */
public interface ExtracteurTexte {

    ResultatExtraction extraire(Path fichier);
}
