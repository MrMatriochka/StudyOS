import { Link } from 'react-router-dom';
import { useMatieres } from '../api/hooks';

export function Matieres() {
  const matieres = useMatieres();

  return (
    <div>
      <h2>Matières</h2>
      {matieres.isLoading && <p className="attenue">Chargement…</p>}
      {matieres.data?.map((m) => (
        <div className="carte" key={m.id} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          {m.couleur && (
            <span style={{ width: 12, height: 12, borderRadius: 3, background: m.couleur }} />
          )}
          <Link to={`/matiere/${m.id}`} style={{ fontWeight: 600 }}>
            {m.libelle}
          </Link>
          {m.aQualifier && <span className="attenue">· à qualifier</span>}
        </div>
      ))}
    </div>
  );
}
