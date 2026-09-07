import { useSearchParams } from 'react-router-dom';
import { useRecherche } from '../api/hooks';
import { urlFichierDocument } from '../api/client';

export function Recherche() {
  const [params] = useSearchParams();
  const q = params.get('q') ?? '';
  const resultats = useRecherche(q);

  return (
    <div>
      <h2>Recherche {q && <span className="attenue">« {q} »</span>}</h2>
      {q.trim() === '' && <p className="attenue">Tape un mot dans la barre en haut.</p>}
      {resultats.isLoading && <p className="attenue">Recherche…</p>}
      {resultats.data?.length === 0 && <p className="attenue">Aucun document ne contient ce terme.</p>}
      {resultats.data?.map((r) => (
        <div className="carte" key={r.documentId}>
          <a href={urlFichierDocument(r.documentId)} target="_blank" rel="noreferrer" style={{ fontWeight: 600 }}>
            {r.titre}
          </a>
          {/* extrait surligne par ts_headline (<b>...</b>), contenu maison */}
          <p className="attenue" dangerouslySetInnerHTML={{ __html: r.extrait }} />
        </div>
      ))}
    </div>
  );
}
