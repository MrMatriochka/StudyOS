package org.example.studyos.suivi.domaine;

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

import org.example.studyos.agenda.domaine.ImportAgenda;
import org.example.studyos.agenda.domaine.Matiere;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Une echeance (rendu, remise, livrable), routee depuis l'agenda au lieu d'une seance.
 * Cle naturelle (uid_externe, echeance) portee par la base.
 * Depend de agenda.domaine (Matiere, ImportAgenda) : couplage assume et minimal,
 * une echeance appartient a une matiere et provient d'un import.
 */
@Entity
@Table(name = "echeance",
        uniqueConstraints = @UniqueConstraint(name = "uk_echeance_occurrence",
                columnNames = {"uid_externe", "echeance"}))
public class Echeance {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private ImportAgenda importAgenda;

    @Column(name = "uid_externe")
    private String uidExterne;

    @Column(nullable = false, length = 500)
    private String libelle;

    @Column(name = "libelle_brut", length = 500)
    private String libelleBrut;

    @Column(name = "echeance", nullable = false)
    private Instant echeance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatEcheance etat = EtatEcheance.A_FAIRE;

    protected Echeance() {
        // requis par JPA
    }

    public Echeance(String uidExterne, String libelle, Instant echeance) {
        this.id = UUID.randomUUID();
        this.uidExterne = uidExterne;
        this.libelle = libelle;
        this.echeance = echeance;
    }

    public void changerEtat(EtatEcheance etat) {
        this.etat = etat;
    }

    public void rattacher(Matiere matiere) {
        this.matiere = matiere;
    }

    public void rattacherImport(ImportAgenda importAgenda) {
        this.importAgenda = importAgenda;
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

    public String getLibelle() {
        return libelle;
    }

    public String getLibelleBrut() {
        return libelleBrut;
    }

    public void setLibelleBrut(String libelleBrut) {
        this.libelleBrut = libelleBrut;
    }

    public Instant getEcheance() {
        return echeance;
    }

    public EtatEcheance getEtat() {
        return etat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Echeance autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
