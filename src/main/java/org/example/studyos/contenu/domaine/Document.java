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

import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.agenda.domaine.Seance;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Un document de cours rattache a une matiere (et eventuellement a une seance).
 * Le fichier vit sur disque (chemin + hash) ; jamais de BLOB en base.
 * L'echec d'extraction est une donnee (extraction_ok / extraction_err), pas un incident.
 * La colonne generee `recherche` (tsvector) n'est PAS mappee : Postgres la gere seul.
 */
@Entity
@Table(name = "document")
public class Document {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id")
    private Seance seance;

    @Column(nullable = false, length = 500)
    private String titre;

    @Column(name = "nom_original", nullable = false, length = 500)
    private String nomOriginal;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TypeDocument type;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false, length = 500)
    private String chemin;

    @Column(nullable = false, length = 64, unique = true)
    private String hash;

    @Column(name = "taille_octets", nullable = false)
    private long tailleOctets;

    @Column(name = "nb_pages")
    private Integer nbPages;

    @Column(name = "texte_extrait", columnDefinition = "text")
    private String texteExtrait;

    @Column(name = "extraction_ok", nullable = false)
    private boolean extractionOk;

    @Column(name = "extraction_err", length = 500)
    private String extractionErr;

    @Column(name = "ajoute_le", nullable = false)
    private Instant ajouteLe;

    protected Document() {
        // requis par JPA
    }

    public Document(Matiere matiere, String titre, String nomOriginal, TypeDocument type,
                    String extension, String chemin, String hash, long tailleOctets) {
        this.id = UUID.randomUUID();
        this.matiere = matiere;
        this.titre = titre;
        this.nomOriginal = nomOriginal;
        this.type = type;
        this.extension = extension;
        this.chemin = chemin;
        this.hash = hash;
        this.tailleOctets = tailleOctets;
        this.ajouteLe = Instant.now();
    }

    public void renseignerExtraction(boolean ok, String texte, Integer nbPages, String erreur) {
        this.extractionOk = ok;
        this.texteExtrait = texte;
        this.nbPages = nbPages;
        this.extractionErr = erreur;
    }

    public void rattacherSeance(Seance seance) {
        this.seance = seance;
    }

    public void renommer(String titre) {
        this.titre = titre;
    }

    public void changerType(TypeDocument type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public Seance getSeance() {
        return seance;
    }

    public String getTitre() {
        return titre;
    }

    public String getNomOriginal() {
        return nomOriginal;
    }

    public TypeDocument getType() {
        return type;
    }

    public String getExtension() {
        return extension;
    }

    public String getChemin() {
        return chemin;
    }

    public String getHash() {
        return hash;
    }

    public long getTailleOctets() {
        return tailleOctets;
    }

    public Integer getNbPages() {
        return nbPages;
    }

    public String getTexteExtrait() {
        return texteExtrait;
    }

    public boolean isExtractionOk() {
        return extractionOk;
    }

    public String getExtractionErr() {
        return extractionErr;
    }

    public Instant getAjouteLe() {
        return ajouteLe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
