package org.example.studyos.agenda.domaine;

/**
 * Type d'une seance, deduit du libelle a l'import.
 * Ordre de priorite (brief §5.4) : EXAMEN/SOUTENANCE &gt; TP &gt; TD &gt; CM &gt; INCONNU.
 * TD_TP correspond au cas « BD - TD/TP ».
 */
public enum TypeSeance {
    CM,
    TD,
    TP,
    TD_TP,
    EXAMEN,
    SOUTENANCE,
    INCONNU
}
