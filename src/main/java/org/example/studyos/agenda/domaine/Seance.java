package org.example.studyos.agenda.domaine;

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

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Une occurrence de cours, apres developpement de la recurrence.
 * Cle naturelle (uid_externe, debut) portee par la base (brief §2 piege n°1).
 * matiere nullable : une seance non rattachee reste valide, en attente de qualification.
 * libelle_brut et salle_brute toujours conserves : re-normaliser sans re-importer.
 */
@Entity
@Table(name = "seance",
        uniqueConstraints = @UniqueConstraint(name = "uk_seance_occurrence",
                columnNames = {"uid_externe", "debut"}))
public class Seance {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private ImportAgenda importAgenda;

    @Column(name = "uid_externe", nullable = false)
    private String uidExterne;

    @Column(name = "libelle_brut", nullable = false, length = 500)
    private String libelleBrut;

    @Column(nullable = false)
    private Instant debut;

    @Column(nullable = false)
    private Instant fin;

    @Column(name = "journee_entiere", nullable = false)
    private boolean journeeEntiere;

    @Column(length = 100)
    private String salle;

    @Column(name = "salle_brute", length = 100)
    private String salleBrute;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private TypeSeance type;

    @Column(nullable = false)
    private boolean annulee;

    protected Seance() {
        // requis par JPA
    }

    public Seance(String uidExterne, String libelleBrut, Instant debut, Instant fin) {
        this.id = UUID.randomUUID();
        this.uidExterne = uidExterne;
        this.libelleBrut = libelleBrut;
        this.debut = debut;
        this.fin = fin;
    }

    public void rattacher(Matiere matiere) {
        this.matiere = matiere;
    }

    public void rattacherImport(ImportAgenda importAgenda) {
        this.importAgenda = importAgenda;
    }

    public void marquerAnnulee() {
        this.annulee = true;
    }

    public UUID getId() {
        return id;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public ImportAgenda getImportAgenda() {
        return importAgenda;
    }

    public String getUidExterne() {
        return uidExterne;
    }

    public String getLibelleBrut() {
        return libelleBrut;
    }

    public Instant getDebut() {
        return debut;
    }

    public void setFin(Instant fin) {
        this.fin = fin;
    }

    public Instant getFin() {
        return fin;
    }

    public boolean isJourneeEntiere() {
        return journeeEntiere;
    }

    public void setJourneeEntiere(boolean journeeEntiere) {
        this.journeeEntiere = journeeEntiere;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getSalleBrute() {
        return salleBrute;
    }

    public void setSalleBrute(String salleBrute) {
        this.salleBrute = salleBrute;
    }

    public TypeSeance getType() {
        return type;
    }

    public void setType(TypeSeance type) {
        this.type = type;
    }

    public boolean isAnnulee() {
        return annulee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seance autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
