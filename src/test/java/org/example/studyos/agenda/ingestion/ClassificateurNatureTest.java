package org.example.studyos.agenda.ingestion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassificateurNatureTest {

    @ParameterizedTest
    @DisplayName("Rendus / remises -> ECHEANCE")
    @ValueSource(strings = {
            "Rendu TP Socket /GRPC",
            "Rendu Dossier CSI",
            "Rendu TP2 Test Unitaire",
            "App d'entreprise - remise projet",
            "Rendu dossier Pilotage pj",
    })
    void rendusEtRemises(String libelle) {
        assertEquals(Nature.ECHEANCE, ClassificateurNature.classer(libelle, false));
    }

    @ParameterizedTest
    @DisplayName("Journee entiere + mot repere -> REPERE")
    @ValueSource(strings = {
            "Semaine d'exam ?",
            "Semaine Examen",
            "Vacances - Révision",
            "Semaine des JNM",
    })
    void reperesJourneeEntiere(String libelle) {
        assertEquals(Nature.REPERE, ClassificateurNature.classer(libelle, true));
    }

    @Test
    @DisplayName("Le meme mot repere SANS journee entiere reste une SEANCE")
    void repereSansJourneeEntiereEstSeance() {
        assertEquals(Nature.SEANCE, ClassificateurNature.classer("Semaine Examen", false));
    }

    @ParameterizedTest
    @DisplayName("Cours ordinaires et examens de cours -> SEANCE")
    @ValueSource(strings = {
            "IA",
            "NoSQL",
            "BD - CM",
            "Conception SI - TP",
            "Examen Qualité des SI",
            "Soutenance Qualité SI",
    })
    void coursEtExamens(String libelle) {
        assertEquals(Nature.SEANCE, ClassificateurNature.classer(libelle, false));
    }
}
