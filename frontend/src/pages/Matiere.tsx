import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  useDeposerDocument,
  useDocuments,
  useMatiere,
  usePropositionSeance,
  useSeancesDeMatiere,
  useSupprimerDocument,
} from '../api/hooks';
import type { Document, Seance } from '../api/types';
import { urlFichierDocument } from '../api/client';
import { jourEtHeure } from '../format';

export function Matiere() {
  const { id = '' } = useParams();
  const matiere = useMatiere(id);
  const seances = useSeancesDeMatiere(id);
  const documents = useDocuments(id);

  return (
    <div>
      <section className="carte">
        <h2 style={{ margin: 0 }}>{matiere.data?.libelle ?? '…'}</h2>
        <div className="attenue">
          {matiere.data?.coefficient != null && <>coef {matiere.data.coefficient} · </>}
          {matiere.data?.semestre && <>{matiere.data.semestre} · </>}
          {matiere.data?.dateExamen ? <>examen le {matiere.data.dateExamen}</> : 'pas de date d’examen'}
        </div>
      </section>

      <ZoneDepot matiereId={id} seances={seances.data ?? []} />

      <section className="carte">
        <h3>Documents {documents.data && <span className="attenue">({documents.data.length})</span>}</h3>
        {documents.isLoading && <p className="attenue">Chargement…</p>}
        {documents.data?.length === 0 && <p className="attenue">Aucun document déposé.</p>}
        {documents.data?.map((d) => (
          <LigneDocument key={d.id} document={d} matiereId={id} />
        ))}
      </section>

      <section className="carte">
        <h3>Séances</h3>
        {seances.data?.length === 0 && <p className="attenue">Aucune séance.</p>}
        {seances.data?.map((s) => (
          <div key={s.id} className="attenue" style={{ padding: '0.15rem 0' }}>
            {jourEtHeure(s.debut)}
            {s.salle && <> · {s.salle}</>}
            {s.type && <> · {s.type}</>}
          </div>
        ))}
      </section>
    </div>
  );
}

function ZoneDepot({ matiereId, seances }: { matiereId: string; seances: Seance[] }) {
  const proposition = usePropositionSeance(matiereId);
  const deposer = useDeposerDocument(matiereId);
  const [fichier, setFichier] = useState<File | null>(null);
  const [seanceId, setSeanceId] = useState('');
  const [survol, setSurvol] = useState(false);

  // Pre-remplir avec la seance proposee (l'utilisateur peut changer/vider).
  useEffect(() => {
    if (proposition.data) {
      setSeanceId(proposition.data.id);
    }
  }, [proposition.data]);

  function deposerFichier() {
    if (!fichier) return;
    deposer.mutate(
      { fichier, seanceId: seanceId || undefined },
      { onSuccess: () => setFichier(null) },
    );
  }

  return (
    <section className="carte">
      <h3>Déposer un document</h3>
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setSurvol(true);
        }}
        onDragLeave={() => setSurvol(false)}
        onDrop={(e) => {
          e.preventDefault();
          setSurvol(false);
          const f = e.dataTransfer.files?.[0];
          if (f) setFichier(f);
        }}
        style={{
          border: `2px dashed ${survol ? 'var(--accent)' : 'var(--bord)'}`,
          borderRadius: 8,
          padding: '1.25rem',
          textAlign: 'center',
          background: survol ? 'var(--accent-doux)' : 'transparent',
        }}
      >
        {fichier ? (
          <strong>{fichier.name}</strong>
        ) : (
          <span className="attenue">Glisse un fichier ici, ou</span>
        )}
        <div style={{ marginTop: '0.5rem' }}>
          <input type="file" onChange={(e) => setFichier(e.target.files?.[0] ?? null)} />
        </div>
      </div>

      <div style={{ marginTop: '0.75rem', display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
        <label className="attenue">Rattacher à la séance :</label>
        <select value={seanceId} onChange={(e) => setSeanceId(e.target.value)}>
          <option value="">— aucune —</option>
          {seances.map((s) => (
            <option key={s.id} value={s.id}>
              {jourEtHeure(s.debut)}
              {s.salle ? ` · ${s.salle}` : ''}
            </option>
          ))}
        </select>
        <button className="primaire" disabled={!fichier || deposer.isPending} onClick={deposerFichier}>
          {deposer.isPending ? 'Dépôt…' : 'Déposer'}
        </button>
      </div>

      {deposer.data && (
        <p className="attenue">
          {deposer.data.dejaPresent
            ? 'Ce fichier était déjà présent (même contenu).'
            : deposer.data.document.extractionOk
              ? `Ajouté, texte extrait (${deposer.data.document.nbPages ?? '?'} pages).`
              : `Ajouté, mais extraction impossible : ${deposer.data.document.extractionErr}`}
        </p>
      )}
      {deposer.isError && (
        <p style={{ color: 'crimson' }}>
          Échec du dépôt : {(deposer.error as Error)?.message ?? 'erreur inconnue'}
        </p>
      )}
    </section>
  );
}

function LigneDocument({ document, matiereId }: { document: Document; matiereId: string }) {
  const supprimer = useSupprimerDocument(matiereId);
  return (
    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', padding: '0.25rem 0' }}>
      <a href={urlFichierDocument(document.id)} target="_blank" rel="noreferrer" style={{ flex: 1, fontWeight: 600 }}>
        {document.titre}
      </a>
      <span className="attenue">{document.type}</span>
      {!document.extractionOk && <span title={document.extractionErr ?? ''}>⚠️</span>}
      <button
        className="danger"
        disabled={supprimer.isPending}
        onClick={() => {
          if (window.confirm(`Supprimer « ${document.titre} » ?`)) supprimer.mutate(document.id);
        }}
      >
        Supprimer
      </button>
    </div>
  );
}
