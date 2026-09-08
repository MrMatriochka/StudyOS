package org.example.studyos.revision.depot;

import org.example.studyos.revision.domaine.Carte;
import org.example.studyos.revision.domaine.StatutCarte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CarteRepository extends JpaRepository<Carte, UUID> {

    List<Carte> findByNotionIdOrderByCreeeLeAsc(UUID notionId);

    /** Marqueur « trou de revision » cote front : notion sans carte. */
    long countByNotionId(UUID notionId);

    // --- Session de revision : cartes dues (echeance <= aujourd'hui) ---
    List<Carte> findByStatutAndProchaineEcheanceLessThanEqualOrderByProchaineEcheanceAsc(
            StatutCarte statut, LocalDate aujourdhui);

    List<Carte> findByStatutAndProchaineEcheanceLessThanEqualAndNotion_Matiere_IdOrderByProchaineEcheanceAsc(
            StatutCarte statut, LocalDate aujourdhui, UUID matiereId);

    // --- Session de revision : cartes nouvelles (jamais planifiees) ---
    List<Carte> findByStatutAndProchaineEcheanceIsNullOrderByCreeeLeAsc(StatutCarte statut);

    List<Carte> findByStatutAndProchaineEcheanceIsNullAndNotion_Matiere_IdOrderByCreeeLeAsc(
            StatutCarte statut, UUID matiereId);

    // --- Score de maitrise : cartes validees d'une matiere ---
    List<Carte> findByStatutAndNotion_Matiere_Id(StatutCarte statut, UUID matiereId);

    /** Notions couvertes = ayant au moins une carte validee (numerateur couverture). */
    @org.springframework.data.jpa.repository.Query("""
            select count(distinct c.notion.id) from Carte c
            where c.statut = :statut and c.notion.matiere.id = :matiereId
            """)
    long compterNotionsCouvertes(StatutCarte statut, UUID matiereId);
}
