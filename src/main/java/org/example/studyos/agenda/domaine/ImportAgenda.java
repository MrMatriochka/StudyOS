package org.example.studyos.agenda.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Trace d'un import de fichier agenda. Sert au debogage (brief §3) :
 * remonter d'une seance fausse a l'echelle a l'import fautif, et compter
 * crees / modifies / inchanges / annules.
 */
@Entity
@Table(name = "import_agenda")
public class ImportAgenda {

    @Id
    private UUID id;

    @Column(nullable = false, length = 20)
    private String source;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "hash_fichier", length = 64)
    private String hashFichier;

    @Column(name = "execute_le", nullable = false)
    private Instant executeLe;

    @Column(name = "nb_crees", nullable = false)
    private int nbCrees;

    @Column(name = "nb_modifies", nullable = false)
    private int nbModifies;

    @Column(name = "nb_inchanges", nullable = false)
    private int nbInchanges;

    @Column(name = "nb_annules", nullable = false)
    private int nbAnnules;

    protected ImportAgenda() {
        // requis par JPA
    }

    public ImportAgenda(String source, String nomFichier, String hashFichier) {
        this.id = UUID.randomUUID();
        this.source = source;
        this.nomFichier = nomFichier;
        this.hashFichier = hashFichier;
        this.executeLe = Instant.now();
    }

    public void compterCree() {
        nbCrees++;
    }

    public void compterModifie() {
        nbModifies++;
    }

    public void compterInchange() {
        nbInchanges++;
    }

    public void compterAnnule() {
        nbAnnules++;
    }

    public UUID getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public String getHashFichier() {
        return hashFichier;
    }

    public Instant getExecuteLe() {
        return executeLe;
    }

    public int getNbCrees() {
        return nbCrees;
    }

    public int getNbModifies() {
        return nbModifies;
    }

    public int getNbInchanges() {
        return nbInchanges;
    }

    public int getNbAnnules() {
        return nbAnnules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportAgenda autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
