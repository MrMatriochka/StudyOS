import { useMemo, useState } from 'react';
import { useSeances } from '../api/hooks';
import type { Seance } from '../api/types';
import { heure } from '../format';

const HEURE_DEBUT = 8;
const HEURE_FIN = 20;
const PX_PAR_HEURE = 48;
const JOURS = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi'];

// Hypothese assumee : le navigateur est en Europe/Paris (app perso, locale).
// On raisonne donc en heure locale pour le placement dans la grille.

export function Semaine() {
  const [refJour, setRefJour] = useState(() => new Date());

  const { lundi, finSemaine } = useMemo(() => {
    const l = new Date(refJour);
    const decalage = (l.getDay() + 6) % 7; // lundi = 0
    l.setDate(l.getDate() - decalage);
    l.setHours(0, 0, 0, 0);
    const f = new Date(l);
    f.setDate(l.getDate() + 5); // jusqu'a samedi 00:00 (couvre lun-ven)
    return { lundi: l, finSemaine: f };
  }, [refJour]);

  const seances = useSeances(lundi.toISOString(), finSemaine.toISOString());

  const datesJours = useMemo(
    () => JOURS.map((_, i) => new Date(lundi.getTime() + i * 86400000)),
    [lundi],
  );

  function decalerSemaine(deltaJours: number) {
    setRefJour((d) => new Date(d.getTime() + deltaJours * 86400000));
  }

  const hauteurGrille = (HEURE_FIN - HEURE_DEBUT) * PX_PAR_HEURE;
  const heures = Array.from({ length: HEURE_FIN - HEURE_DEBUT }, (_, i) => HEURE_DEBUT + i);

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem' }}>
        <button onClick={() => decalerSemaine(-7)}>‹ Semaine précédente</button>
        <button onClick={() => setRefJour(new Date())}>Aujourd'hui</button>
        <button onClick={() => decalerSemaine(7)}>Semaine suivante ›</button>
        <span className="attenue">
          {datesJours[0].toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' })}
          {' – '}
          {datesJours[4].toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })}
        </span>
      </div>

      <div className="carte" style={{ padding: '0.5rem' }}>
        {/* En-tete des jours */}
        <div style={{ display: 'grid', gridTemplateColumns: `48px repeat(5, 1fr)` }}>
          <div />
          {JOURS.map((nom, i) => (
            <div key={nom} style={{ textAlign: 'center', fontWeight: 600, padding: '0.25rem 0' }}>
              {nom}
              <div className="attenue" style={{ fontWeight: 400, fontSize: '0.8rem' }}>
                {datesJours[i].toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' })}
              </div>
            </div>
          ))}
        </div>

        {/* Corps : axe des heures + 5 colonnes */}
        <div style={{ display: 'grid', gridTemplateColumns: `48px repeat(5, 1fr)` }}>
          {/* Axe des heures */}
          <div style={{ position: 'relative', height: hauteurGrille }}>
            {heures.map((h) => (
              <div
                key={h}
                className="attenue"
                style={{ position: 'absolute', top: (h - HEURE_DEBUT) * PX_PAR_HEURE - 6, fontSize: '0.75rem' }}
              >
                {h}h
              </div>
            ))}
          </div>

          {/* Colonnes jours */}
          {datesJours.map((_, indexJour) => (
            <ColonneJour
              key={indexJour}
              hauteur={hauteurGrille}
              seances={(seances.data ?? []).filter((s) => indexJourDe(s.debut) === indexJour)}
            />
          ))}
        </div>
      </div>

      {seances.isLoading && <p className="attenue">Chargement…</p>}
    </div>
  );
}

function ColonneJour({ hauteur, seances }: { hauteur: number; seances: Seance[] }) {
  return (
    <div
      style={{
        position: 'relative',
        height: hauteur,
        borderLeft: '1px solid var(--bord)',
        backgroundImage: `repeating-linear-gradient(to bottom, transparent, transparent ${PX_PAR_HEURE - 1}px, var(--bord) ${PX_PAR_HEURE - 1}px, var(--bord) ${PX_PAR_HEURE}px)`,
      }}
    >
      {seances.map((s) => (
        <BlocSeance key={s.id} seance={s} />
      ))}
    </div>
  );
}

function BlocSeance({ seance }: { seance: Seance }) {
  const debut = heureDecimale(seance.debut);
  const fin = heureDecimale(seance.fin);
  const top = (Math.max(debut, HEURE_DEBUT) - HEURE_DEBUT) * PX_PAR_HEURE;
  const bas = (Math.min(fin, HEURE_FIN) - HEURE_DEBUT) * PX_PAR_HEURE;
  const hauteur = Math.max(bas - top, 20);
  const couleur = seance.matiere?.couleur ?? '#dfe3f2';
  const nom = seance.matiere?.libelle ?? seance.libelleBrut;

  return (
    <div
      title={`${nom} ${heure(seance.debut)}–${heure(seance.fin)}${seance.salle ? ' · ' + seance.salle : ''}`}
      style={{
        position: 'absolute',
        top,
        height: hauteur,
        left: 2,
        right: 2,
        background: couleur,
        borderLeft: '3px solid var(--accent)',
        borderRadius: 4,
        padding: '2px 4px',
        fontSize: '0.75rem',
        overflow: 'hidden',
      }}
    >
      <div style={{ fontWeight: 600, whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
        {nom}
      </div>
      <div className="attenue">
        {heure(seance.debut)}
        {seance.salle && <> · {seance.salle}</>}
      </div>
    </div>
  );
}

function heureDecimale(iso: string): number {
  const d = new Date(iso);
  return d.getHours() + d.getMinutes() / 60;
}

function indexJourDe(iso: string): number {
  return (new Date(iso).getDay() + 6) % 7; // lundi = 0
}
