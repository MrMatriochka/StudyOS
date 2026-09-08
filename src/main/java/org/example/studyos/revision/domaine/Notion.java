package org.example.studyos.revision.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.contenu.domaine.SectionDocument;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Une notion d'une matiere, eventuellement issue d'une section de document.
 * Une notion sans carte est un « trou de revision » (marque cote front).
 */
@Entity
@Table(name = "notion")
public class Notion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_document_id")
    private SectionDocument section;

    @Column(nullable = false, length = 500)
    private String intitule;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "creee_le", nullable = false)
    private Instant creeeLe;

    protected Notion() {
        // requis par JPA
    }

    public Notion(Matiere matiere, String intitule) {
        this.id = UUID.randomUUID();
        this.matiere = matiere;
        this.intitule = intitule;
        this.creeeLe = Instant.now();
    }

    public void renommer(String intitule) {
        this.intitule = intitule;
    }

    public void decrire(String description) {
        this.description = description;
    }

    public void rattacherSection(SectionDocument section) {
        this.section = section;
    }

    public UUID getId() {
        return id;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public SectionDocument getSection() {
        return section;
    }

    public String getIntitule() {
        return intitule;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreeeLe() {
        return creeeLe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notion autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
