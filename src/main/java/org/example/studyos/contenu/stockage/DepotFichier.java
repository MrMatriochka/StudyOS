package org.example.studyos.contenu.stockage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stockage des fichiers sur disque (brief lot 2). Le chemin est derive de l'id,
 * jamais du nom original (noms fournis ingerables et en collision) :
 *   {racine}/{hash[0:2]}/{id}.{extension}
 * Le sous-repertoire a 2 caracteres evite des milliers de fichiers a plat.
 */
@Component
public class DepotFichier {

    private final Path racine;

    public DepotFichier(@Value("${studyos.documents.racine}") String racine) {
        this.racine = Path.of(racine);
    }

    /** Ecrit le contenu et retourne le chemin absolu stocke en base. */
    public String stocker(byte[] contenu, String hash, UUID id, String extension) {
        Path cible = cheminPour(hash, id, extension);
        try {
            Files.createDirectories(cible.getParent());
            Files.write(cible, contenu);
            return cible.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Ecriture du document impossible : " + cible, e);
        }
    }

    public Path chemin(String chemin) {
        return Path.of(chemin);
    }

    public byte[] lire(String chemin) {
        try {
            return Files.readAllBytes(Path.of(chemin));
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du document impossible : " + chemin, e);
        }
    }

    public void supprimer(String chemin) {
        try {
            Files.deleteIfExists(Path.of(chemin));
        } catch (IOException e) {
            throw new UncheckedIOException("Suppression du document impossible : " + chemin, e);
        }
    }

    private Path cheminPour(String hash, UUID id, String extension) {
        String sousDossier = hash.substring(0, 2);
        return racine.resolve(sousDossier).resolve(id + "." + extension);
    }
}
