package org.example.studyos.revision.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Une revision effectuee : journal complet (event sourcing leger). L'etat
 * resultant est fige ici pour pouvoir rejouer l'historique si on change d'algo.
 */
@Entity
@Table(name = "revision")
public class Revision {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carte_id", nullable = false)
    private Carte carte;

    @Column(name = "faite_le", nullable = false)
    private Instant faiteLe;

    @Column(nullable = false)
    private int qualite;

    @Column(nullable = false)
    private int repetitions;

    @Column(name = "intervalle_jours", nullable = false)
    private int intervalleJours;

    @Column(nullable = false)
    private float facilite;

    @Column(name = "duree_ms")
    private Integer dureeMs;

    protected Revision() {
        // requis par JPA
    }

    public Revision(Carte carte, int qualite, int repetitions, int intervalleJours,
                    double facilite, Integer dureeMs) {
        this.id = UUID.randomUUID();
        this.carte = carte;
        this.faiteLe = Instant.now();
        this.qualite = qualite;
        this.repetitions = repetitions;
        this.intervalleJours = intervalleJours;
        this.facilite = (float) facilite;
        this.dureeMs = dureeMs;
    }

    public UUID getId() {
        return id;
    }

    public Carte getCarte() {
        return carte;
    }

    public Instant getFaiteLe() {
        return faiteLe;
    }

    public int getQualite() {
        return qualite;
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

    public Integer getDureeMs() {
        return dureeMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Revision autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
