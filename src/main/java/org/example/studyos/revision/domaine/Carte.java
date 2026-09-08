package org.example.studyos.revision.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Une carte question/reponse rattachee a une notion. Porte son etat de
 * planification COURANT (denormalisation) pour que « cartes dues aujourd'hui »
 * soit une simple lecture indexee.
 */
@Entity
@Table(name = "carte")
public class Carte {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notion_id", nullable = false)
    private Notion notion;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(nullable = false, columnDefinition = "text")
    private String reponse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigineCarte origine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCarte statut = StatutCarte.VALIDEE;

    // --- etat de planification (denormalise) ---
    @Column(nullable = false)
    private int repetitions;

    @Column(name = "intervalle_jours", nullable = false)
    private int intervalleJours;

    @Column(nullable = false)
    private float facilite = 2.5f;

    @Column(name = "derniere_revision")
    private LocalDate derniereRevision;

    @Column(name = "prochaine_echeance")
    private LocalDate prochaineEcheance;

    @Column(name = "creee_le", nullable = false)
    private Instant creeeLe;

    protected Carte() {
        // requis par JPA
    }

    public Carte(Notion notion, String question, String reponse, OrigineCarte origine) {
        this.id = UUID.randomUUID();
        this.notion = notion;
        this.question = question;
        this.reponse = reponse;
        this.origine = origine;
        this.creeeLe = Instant.now();
    }

    public void modifier(String question, String reponse) {
        this.question = question;
        this.reponse = reponse;
    }

    public void changerStatut(StatutCarte statut) {
        this.statut = statut;
    }

    /** Applique un nouvel etat de planification (issu du SM-2) apres une revision. */
    public void appliquerPlanification(int repetitions, int intervalleJours, double facilite,
                                       LocalDate derniereRevision, LocalDate prochaineEcheance) {
        this.repetitions = repetitions;
        this.intervalleJours = intervalleJours;
        this.facilite = (float) facilite;
        this.derniereRevision = derniereRevision;
        this.prochaineEcheance = prochaineEcheance;
    }

    public UUID getId() {
        return id;
    }

    public Notion getNotion() {
        return notion;
    }

    public String getQuestion() {
        return question;
    }

    public String getReponse() {
        return reponse;
    }

    public OrigineCarte getOrigine() {
        return origine;
    }

    public StatutCarte getStatut() {
        return statut;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public int getIntervalleJours() {
        return intervalleJours;
    }

    public double getFacilite() {
        return facilite;
    }

    public LocalDate getDerniereRevision() {
        return derniereRevision;
    }

    public LocalDate getProchaineEcheance() {
        return prochaineEcheance;
    }

    public Instant getCreeeLe() {
        return creeeLe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carte autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
