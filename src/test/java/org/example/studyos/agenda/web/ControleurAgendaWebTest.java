package org.example.studyos.agenda.web;

import org.example.studyos.agenda.domaine.Matiere;
import org.example.studyos.agenda.domaine.Seance;
import org.example.studyos.agenda.domaine.TypeSeance;
import org.example.studyos.agenda.service.ServiceAgenda;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test tranche web (sans base) : binding des Instant en parametres,
 * serialisation DTO, et code 204 quand aucune seance.
 */
@WebMvcTest(ControleurAgenda.class)
class ControleurAgendaWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServiceAgenda serviceAgenda;

    private Seance seanceExemple() {
        Matiere matiere = Matiere.aQualifier("IA");
        Seance seance = new Seance("uid-1", "IA",
                Instant.parse("2026-03-13T14:45:00Z"), Instant.parse("2026-03-13T16:45:00Z"));
        seance.setType(TypeSeance.INCONNU);
        seance.setSalle("U2-114");
        seance.rattacher(matiere);
        return seance;
    }

    @Test
    void surPlageRetourneLesSeances() throws Exception {
        when(serviceAgenda.seancesEntre(any(), any())).thenReturn(List.of(seanceExemple()));

        mvc.perform(get("/api/seances")
                        .param("du", "2026-03-01T00:00:00Z")
                        .param("au", "2026-03-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libelleBrut").value("IA"))
                .andExpect(jsonPath("$[0].salle").value("U2-114"))
                .andExpect(jsonPath("$[0].matiere.libelle").value("IA"))
                .andExpect(jsonPath("$[0].matiere.aQualifier").value(true));
    }

    @Test
    void prochaineRenvoie204SiAucune() throws Exception {
        when(serviceAgenda.prochaineSeance()).thenReturn(Optional.empty());

        mvc.perform(get("/api/seances/prochaine"))
                .andExpect(status().isNoContent());
    }
}
