package org.example.studyos.agenda.ingestion;

import org.example.studyos.agenda.domaine.TypeSeance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DetecteurTypeSeanceTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            // priorite examen/soutenance sur le type de creneau
            "Soutenance Qualité SI          | SOUTENANCE",
            "Examen Qualité des SI          | EXAMEN",
            "IA - CC                        | EXAMEN",
            "Anglais - Oral Exam            | EXAMEN",
            "Examen App Repartie - TD       | EXAMEN",
            // td/tp doit primer sur td et tp isoles
            "BD - TD/TP                     | TD_TP",
            // creneaux simples
            "Opti Linéaire - TP             | TP",
            "TP opti                        | TP",
            "Conception SI - TP             | TP",
            "Qualité SI - TD                | TD",
            "BD - CM                        | CM",
            "Qualité SI - CM                | CM",
            // aucun indicateur
            "IA                             | INCONNU",
            "NoSQL                          | INCONNU",
    })
    @DisplayName("Detection du type par priorite")
    void detecteType(String libelle, TypeSeance attendu) {
        assertEquals(attendu, DetecteurTypeSeance.detecter(libelle));
    }
}
