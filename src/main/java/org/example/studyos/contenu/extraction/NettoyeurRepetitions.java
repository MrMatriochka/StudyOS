package org.example.studyos.contenu.extraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Retire les lignes repetitives (en-tetes, pieds de page, numeros de diapo,
 * copyright) qui polluent chaque section (brief §lot2).
 *
 * Heuristique : une ligne courte (&lt; 80 caracteres) presente dans plus de 70 %
 * des sections est retiree de toutes. Applique seulement au-dela de 5 sections.
 */
public final class NettoyeurRepetitions {

    private static final int MIN_SECTIONS = 5;
    private static final int LONGUEUR_MAX_LIGNE = 80;
    private static final double SEUIL_PRESENCE = 0.70;

    private NettoyeurRepetitions() {
    }

    public static List<Section> nettoyer(List<Section> sections) {
        if (sections.size() <= MIN_SECTIONS) {
            return sections;
        }

        Map<String, Integer> presence = new HashMap<>();
        for (Section section : sections) {
            for (String ligne : lignesDistinctes(section.texte())) {
                if (ligne.length() < LONGUEUR_MAX_LIGNE) {
                    presence.merge(ligne, 1, Integer::sum);
                }
            }
        }

        double minimum = SEUIL_PRESENCE * sections.size();
        Set<String> aRetirer = presence.entrySet().stream()
                .filter(e -> e.getValue() > minimum)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (aRetirer.isEmpty()) {
            return sections;
        }
        return sections.stream()
                .map(section -> section.avecTexte(sansLignes(section.texte(), aRetirer)))
                .toList();
    }

    private static Set<String> lignesDistinctes(String texte) {
        Set<String> lignes = new LinkedHashSet<>();
        for (String ligne : texte.split("\n")) {
            String propre = ligne.strip();
            if (!propre.isEmpty()) {
                lignes.add(propre);
            }
        }
        return lignes;
    }

    private static String sansLignes(String texte, Set<String> aRetirer) {
        return Arrays.stream(texte.split("\n"))
                .filter(ligne -> !aRetirer.contains(ligne.strip()))
                .collect(Collectors.joining("\n"))
                .strip();
    }
}
