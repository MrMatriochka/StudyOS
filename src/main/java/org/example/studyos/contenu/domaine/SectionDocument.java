package org.example.studyos.contenu.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;
import java.util.UUID;

/**
 * Une section d'un document (diapositive, page, titre...), unite exploitee par le lot 3.
 * `notes` (notes du presentateur PPTX) est distincte du `texte`, jamais concatenee :
 * le lot 4 voudra les ponderer differemment.
 */
@Entity
@Table(name = "section_document",
        uniqueConstraints = @UniqueConstraint(name = "uk_section_ordre",
                columnNames = {"document_id", "ordre"}))
public class SectionDocument {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private int ordre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Granularite granularite;

    @Column(length = 500)
    private String titre;

    @Column(nullable = false, columnDefinition = "text")
    private String texte;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "page")
    private Integer page;

    protected SectionDocument() {
        // requis par JPA
    }

    public SectionDocument(Document document, int ordre, Granularite granularite,
                           String titre, String texte, String notes, Integer page) {
        this.id = UUID.randomUUID();
        this.document = document;
        this.ordre = ordre;
        this.granularite = granularite;
        this.titre = titre;
        this.texte = texte;
        this.notes = notes;
        this.page = page;
    }

    public UUID getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public int getOrdre() {
        return ordre;
    }

    public Granularite getGranularite() {
        return granularite;
    }

    public String getTitre() {
        return titre;
    }

    public String getTexte() {
        return texte;
    }

    public String getNotes() {
        return notes;
    }

    public Integer getPage() {
        return page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SectionDocument autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
