package org.example.studyos.revision.planification;

import org.example.studyos.revision.domaine.EtatPlanification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sm2PlanificateurTest {

    private final Sm2Planificateur sm2 = new Sm2Planificateur();
    private final LocalDate jour = LocalDate.of(2026, 3, 1);

    @Test
    @DisplayName("Sequence 4,4,4 : intervalles 1 j, 6 j, puis round(6 x facilite)")
    void sequenceDeReference() {
        EtatPlanification e = EtatPlanification.initial();

        e = sm2.planifier(e, 4, jour);
        assertEquals(1, e.repetitions());
        assertEquals(1, e.intervalleJours());
        assertEquals(2.5, e.facilite(), 1e-9, "q=4 laisse la facilite inchangee");
        assertEquals(jour.plusDays(1), e.prochaineEcheance());

        e = sm2.planifier(e, 4, jour);
        assertEquals(2, e.repetitions());
        assertEquals(6, e.intervalleJours());

        e = sm2.planifier(e, 4, jour);
        assertEquals(3, e.repetitions());
        assertEquals(15, e.intervalleJours(), "round(6 x 2.5) = 15");
        assertEquals(jour.plusDays(15), e.prochaineEcheance());
    }

    @Test
    @DisplayName("Echec (q=0) : retour a repetitions=0 et intervalle=1 j")
    void echecReinitialise() {
        EtatPlanification e = EtatPlanification.initial();
        e = sm2.planifier(e, 4, jour);
        e = sm2.planifier(e, 4, jour);
        e = sm2.planifier(e, 4, jour); // rep=3, intervalle=15, facilite=2.5

        e = sm2.planifier(e, 0, jour);

        assertEquals(0, e.repetitions());
        assertEquals(1, e.intervalleJours());
        assertEquals(jour.plusDays(1), e.prochaineEcheance());
        // facilite 2.5 + (0.1 - 5*(0.08 + 5*0.02)) = 2.5 - 0.8 = 1.7
        assertEquals(1.7, e.facilite(), 1e-9);
    }

    @Test
    @DisplayName("Plancher : 10 echecs consecutifs ne descendent jamais sous 1.3")
    void plancherFacilite() {
        EtatPlanification e = EtatPlanification.initial();
        for (int i = 0; i < 10; i++) {
            e = sm2.planifier(e, 0, jour);
            assertTrue(e.facilite() >= 1.3, "facilite jamais sous le plancher");
        }
        assertEquals(1.3, e.facilite(), 1e-9);
    }
}
