import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useEnregistrerRevision, useSession } from '../api/hooks';
import type { SessionCarte } from '../api/types';

// Mapping des 4 boutons vers l'echelle SM-2 (identique au back).
const BOUTONS = [
  { touche: '1', libelle: 'Encore', qualite: 0, champ: 'prevuEncore' as const },
  { touche: '2', libelle: 'Difficile', qualite: 3, champ: 'prevuDifficile' as const },
  { touche: '3', libelle: 'Bien', qualite: 4, champ: 'prevuBien' as const },
  { touche: '4', libelle: 'Facile', qualite: 5, champ: 'prevuFacile' as const },
];

export function Revision() {
  const [params] = useSearchParams();
  const matiereId = params.get('matiereId') ?? undefined;
  const session = useSession(matiereId);
  const enregistrer = useEnregistrerRevision();

  // Snapshot local : la file ne bouge pas pendant la session.
  const [file, setFile] = useState<SessionCarte[] | null>(null);
  const [index, setIndex] = useState(0);
  const [revele, setRevele] = useState(false);

  useEffect(() => {
    if (session.data && file === null) setFile(session.data);
  }, [session.data, file]);

  const carte = file?.[index];

  const noter = useCallback(
    (qualite: number) => {
      if (!carte) return;
      enregistrer.mutate({ carteId: carte.id, qualite });
      setRevele(false);
      setIndex((i) => i + 1);
    },
    [carte, enregistrer],
  );

  // Clavier : Espace/Entree revele, 1-4 note.
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (!carte) return;
      if (!revele && (e.key === ' ' || e.key === 'Enter')) {
        e.preventDefault();
        setRevele(true);
        return;
      }
      if (revele) {
        const bouton = BOUTONS.find((b) => b.touche === e.key);
        if (bouton) {
          e.preventDefault();
          noter(bouton.qualite);
        }
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [carte, revele, noter]);

  if (session.isLoading || file === null) {
    return <p className="attenue">Chargement de la session…</p>;
  }
  if (file.length === 0) {
    return <div className="carte"><h2>Révision</h2><p className="attenue">Rien à réviser aujourd'hui. 🎉</p></div>;
  }
  if (!carte) {
    return (
      <div className="carte">
        <h2>Session terminée 🎉</h2>
        <p className="attenue">{file.length} carte{file.length > 1 ? 's' : ''} revue{file.length > 1 ? 's' : ''}.</p>
      </div>
    );
  }

  return (
    <div>
      <p className="attenue">{file.length - index} restante{file.length - index > 1 ? 's' : ''}</p>

      <div className="carte" style={{ minHeight: 160, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <div style={{ fontSize: '1.3rem', fontWeight: 600 }}>{carte.question}</div>
        {revele && (
          <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid var(--bord)', fontSize: '1.1rem' }}>
            {carte.reponse}
          </div>
        )}
      </div>

      {!revele ? (
        <button className="primaire" onClick={() => setRevele(true)}>
          Révéler <span className="attenue">(Espace)</span>
        </button>
      ) : (
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          {BOUTONS.map((b) => (
            <button key={b.touche} onClick={() => noter(b.qualite)} style={{ flex: 1, minWidth: 120 }}>
              <div>
                <strong>{b.touche}</strong> · {b.libelle}
              </div>
              <div className="attenue">{carte[b.champ]} j</div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
