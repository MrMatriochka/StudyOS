package org.example.studyos.agenda.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Une matiere du cursus. Peut etre creee automatiquement a l'import
 * (a_qualifier = true) quand aucun alias ne correspond au libelle.
 */
@Entity
@Table(name = "matiere")
public class Matiere {

    @Id
    private UUID id;

    @Column(length = 50)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(length = 20)
    private String semestre;

    private Integer coefficient;

    @Column(length = 7)
    private String couleur;

    @Column(name = "date_examen")
    private LocalDate dateExamen;

    // Colonne 'a_qualifier' : nom de champ different pour eviter le piege
    // JavaBeans (minuscule seule + majuscule) qui casse les query methods Spring Data.
    @Column(name = "a_qualifier", nullable = false)
    private boolean enAttenteQualification;

    protected Matiere() {
        // requis par JPA
    }

    private Matiere(UUID id, String libelle, boolean enAttenteQualification) {
        this.id = id;
        this.libelle = libelle;
        this.enAttenteQualification = enAttenteQualification;
    }

    /** Matiere creee a la main, deja qualifiee. */
    public static Matiere qualifiee(String libelle) {
        return new Matiere(UUID.randomUUID(), libelle, false);
    }

    /** Matiere creee automatiquement a l'import, en attente de qualification. */
    public static Matiere aQualifier(String libelle) {
        return new Matiere(UUID.randomUUID(), libelle, true);
    }

    public void renommer(String libelle) {
        this.libelle = libelle;
    }

    public void marquerQualifiee() {
        this.enAttenteQualification = false;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public Integer getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(Integer coefficient) {
        this.coefficient = coefficient;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public LocalDate getDateExamen() {
        return dateExamen;
    }

    public void setDateExamen(LocalDate dateExamen) {
        this.dateExamen = dateExamen;
    }

    public boolean isEnAttenteQualification() {
        return enAttenteQualification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matiere autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
