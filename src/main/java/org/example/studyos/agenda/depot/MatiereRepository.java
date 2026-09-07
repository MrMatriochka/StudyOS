package org.example.studyos.agenda.depot;

import org.example.studyos.agenda.domaine.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatiereRepository extends JpaRepository<Matiere, UUID> {

    /** Matieres auto-creees a l'import, ecran de qualification. */
    List<Matiere> findByEnAttenteQualificationTrueOrderByLibelleAsc();
}
