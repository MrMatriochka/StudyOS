package org.example.studyos.agenda.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Lie un libelle normalise a une matiere. Alimente semi-manuellement :
 * c'est le pivot de la qualification (brief §2 piege n°3, pas de regex).
 */
@Entity
@Table(name = "alias_matiere")
public class AliasMatiere {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @Column(name = "libelle_norm", nullable = false, length = 500, unique = true)
    private String libelleNorm;

    protected AliasMatiere() {
        // requis par JPA
    }

    public AliasMatiere(Matiere matiere, String libelleNorm) {
        this.id = UUID.randomUUID();
        this.matiere = matiere;
        this.libelleNorm = libelleNorm;
    }

    /** Utilise par la fusion : rediriger l'alias vers la matiere conservee. */
    public void reaffecter(Matiere matiere) {
        this.matiere = matiere;
    }

    public UUID getId() {
        return id;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public String getLibelleNorm() {
        return libelleNorm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AliasMatiere autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
