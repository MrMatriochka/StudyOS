package org.example.studyos.agenda.service;

import org.example.studyos.agenda.depot.AliasMatiereRepository;
import org.example.studyos.agenda.depot.MatiereRepository;
import org.example.studyos.agenda.domaine.AliasMatiere;
import org.example.studyos.agenda.domaine.Matiere;
import org.springframework.stereotype.Component;

/**
 * Resout une matiere depuis un libelle normalise (brief §5.6).
 * Cherche dans alias_matiere ; si absent, cree une matiere « a qualifier »
 * et son alias. Ne devine jamais : la vraie identite sera fixee a la main.
 */
@Component
public class ResolveurMatiere {

    private final MatiereRepository matieres;
    private final AliasMatiereRepository alias;

    public ResolveurMatiere(MatiereRepository matieres, AliasMatiereRepository alias) {
        this.matieres = matieres;
        this.alias = alias;
    }

    /**
     * @param libelleNorm  cle normalisee (sortie de NormalisateurLibelle)
     * @param libelleAffiche libelle brut, sert de nom lisible a la matiere auto-creee
     */
    public Matiere resoudre(String libelleNorm, String libelleAffiche) {
        return alias.findByLibelleNorm(libelleNorm)
                .map(AliasMatiere::getMatiere)
                .orElseGet(() -> creer(libelleNorm, libelleAffiche));
    }

    private Matiere creer(String libelleNorm, String libelleAffiche) {
        Matiere matiere = Matiere.aQualifier(libelleAffiche);
        matieres.save(matiere);
        alias.save(new AliasMatiere(matiere, libelleNorm));
        return matiere;
    }
}
