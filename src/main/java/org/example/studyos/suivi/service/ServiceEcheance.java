package org.example.studyos.suivi.service;

import org.example.studyos.suivi.depot.EcheanceRepository;
import org.example.studyos.suivi.domaine.Echeance;
import org.example.studyos.suivi.domaine.EtatEcheance;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Suivi des echeances (brief §6) : lister a venir, changer l'etat. */
@Service
public class ServiceEcheance {

    private final EcheanceRepository echeances;

    public ServiceEcheance(EcheanceRepository echeances) {
        this.echeances = echeances;
    }

    @Transactional(readOnly = true)
    public List<Echeance> aVenir(Instant apres) {
        return echeances.findByEcheanceAfterOrderByEcheanceAsc(apres);
    }

    @Transactional
    public Echeance changerEtat(UUID id, EtatEcheance etat) {
        Echeance echeance = echeances.findWithMatiereById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Echeance introuvable : " + id));
        echeance.changerEtat(etat);
        return echeance;
    }
}
