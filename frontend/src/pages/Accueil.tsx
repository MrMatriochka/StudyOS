import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useEcheances, useProchaineSeance, useSeances, useSession } from '../api/hooks';
import type { Echeance, Seance } from '../api/types';
import { heure, jour, jourEtHeure, tempsRestant } from '../format';

export function Accueil() {
  // Bornes figees au montage pour des clefs de requete stables.
  const { debutJour, finJour, maintenant, dans14j } = useMemo(() => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    const f = new Date();
    f.setHours(23, 59, 59, 999);
    const plus14 = new Date();
    plus14.setDate(plus14.getDate() + 14);
    return {
      debutJour: d.toISOString(),
      finJour: f.toISOString(),
      maintenant: new Date().toISOString(),
      dans14j: plus14.toISOString(),
    };
  }, []);

  const prochaine = useProchaineSeance();
  const duJour = useSeances(debutJour, finJour);
  const echeances = useEcheances(maintenant);
  const session = useSession();

  const echeances14j = (echeances.data ?? []).filter((e) => e.echeance <= dans14j);
  const aReviser = session.data?.length ?? 0;

  return (
    <div>
      {aReviser > 0 && (
        <Link to="/revision" style={{ display: 'block' }}>
          <section className="carte" style={{ background: 'var(--accent)', color: '#fff', borderColor: 'var(--accent)' }}>
            <strong style={{ fontSize: '1.2rem' }}>
              {aReviser} carte{aReviser > 1 ? 's' : ''} à réviser aujourd'hui →
            </strong>
          </section>
        </Link>
      )}

      <CarteProchainCours seance={prochaine.data} chargement={prochaine.isLoading} />

      <section className="carte">
        <h3>Aujourd'hui</h3>
        {duJour.isLoading && <p className="attenue">Chargement…</p>}
        {duJour.data?.length === 0 && <p className="attenue">Aucune séance aujourd'hui.</p>}
        {duJour.data?.map((s) => (
          <LigneSeance key={s.id} seance={s} />
        ))}
      </section>

      <section className="carte">
        <h3>Échéances (14 prochains jours)</h3>
        {echeances.isLoading && <p className="attenue">Chargement…</p>}
        {echeances14j.length === 0 && <p className="attenue">Rien à rendre. 🎉</p>}
        {echeances14j.map((e) => (
          <LigneEcheance key={e.id} echeance={e} />
        ))}
      </section>
    </div>
  );
}

function CarteProchainCours({ seance, chargement }: { seance: Seance | null | undefined; chargement: boolean }) {
  if (chargement) {
    return (
      <section className="carte">
        <p className="attenue">Chargement…</p>
      </section>
    );
  }
  if (!seance) {
    return (
      <section className="carte">
        <span className="attenue">Prochain cours</span>
        <p>Aucun cours à venir.</p>
      </section>
    );
  }
  const nom = seance.matiere?.libelle ?? seance.libelleBrut;
  return (
    <section className="carte" style={{ borderColor: 'var(--accent)' }}>
      <span className="attenue">Prochain cours · {tempsRestant(seance.debut)}</span>
      <div style={{ fontSize: '1.6rem', fontWeight: 700, margin: '0.25rem 0' }}>{nom}</div>
      <div>
        {jourEtHeure(seance.debut)} → {heure(seance.fin)}
        {seance.salle && <> · salle {seance.salle}</>}
      </div>
    </section>
  );
}

function LigneSeance({ seance }: { seance: Seance }) {
  const nom = seance.matiere?.libelle ?? seance.libelleBrut;
  return (
    <div style={{ display: 'flex', gap: '0.75rem', padding: '0.3rem 0' }}>
      <span style={{ minWidth: '110px' }} className="attenue">
        {seance.journeeEntiere ? 'journée' : `${heure(seance.debut)}–${heure(seance.fin)}`}
      </span>
      <span style={{ flex: 1 }}>{nom}</span>
      {seance.salle && <span className="attenue">{seance.salle}</span>}
    </div>
  );
}

function LigneEcheance({ echeance }: { echeance: Echeance }) {
  return (
    <div style={{ display: 'flex', gap: '0.75rem', padding: '0.3rem 0' }}>
      <span style={{ minWidth: '110px' }} className="attenue">
        {jour(echeance.echeance)}
      </span>
      <span style={{ flex: 1 }}>{echeance.libelle}</span>
      <span className="attenue">{echeance.etat}</span>
    </div>
  );
}
