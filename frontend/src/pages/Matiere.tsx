import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  useCartes,
  useCreerCarte,
  useCreerNotion,
  useDeposerDocument,
  useDocuments,
  useMaitrise,
  useMatiere,
  useNotions,
  usePropositionSeance,
  useSeancesDeMatiere,
  useSupprimerCarte,
  useSupprimerDocument,
  useSupprimerNotion,
} from '../api/hooks';
import type { Document, Notion, Seance } from '../api/types';
import { urlFichierDocument } from '../api/client';
import { jourEtHeure } from '../format';

export function Matiere() {
  const { id = '' } = useParams();
  const matiere = useMatiere(id);
  const seances = useSeancesDeMatiere(id);
  const documents = useDocuments(id);
  const maitrise = useMaitrise(id);

  return (
    <div>
      <section className="carte">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          <h2 style={{ margin: 0 }}>{matiere.data?.libelle ?? '…'}</h2>
          {maitrise.data && (
            <span className="attenue">
              · maîtrise{' '}
              {maitrise.data.evaluee && maitrise.data.maitrise != null
                ? `${Math.round(maitrise.data.maitrise * 100)}%`
                : 'non évaluée'}
            </span>
          )}
          <Link to={`/revision?matiereId=${id}`} style={{ marginLeft: 'auto' }}>
            <button className="primaire">Réviser cette matière</button>
          </Link>
        </div>
        <div className="attenue" style={{ marginTop: '0.35rem' }}>
          {matiere.data?.coefficient != null && <>coef {matiere.data.coefficient} · </>}
          {matiere.data?.semestre && <>{matiere.data.semestre} · </>}
          {matiere.data?.dateExamen ? <>examen le {matiere.data.dateExamen}</> : 'pas de date d’examen'}
        </div>
      </section>

      <SectionNotions matiereId={id} />

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

function SectionNotions({ matiereId }: { matiereId: string }) {
  const notions = useNotions(matiereId);
  const creer = useCreerNotion(matiereId);
  const [intitule, setIntitule] = useState('');

  return (
    <section className="carte">
      <h3>Notions {notions.data && <span className="attenue">({notions.data.length})</span>}</h3>

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
        <input
          placeholder="Nouvelle notion…"
          value={intitule}
          onChange={(e) => setIntitule(e.target.value)}
          style={{ flex: 1 }}
        />
        <button
          className="primaire"
          disabled={!intitule.trim() || creer.isPending}
          onClick={() => creer.mutate(intitule.trim(), { onSuccess: () => setIntitule('') })}
        >
          Ajouter
        </button>
      </div>

      {notions.data?.length === 0 && <p className="attenue">Aucune notion. Découpe ta matière ici.</p>}
      {notions.data?.map((n) => (
        <LigneNotion key={n.id} notion={n} matiereId={matiereId} />
      ))}
    </section>
  );
}

function LigneNotion({ notion, matiereId }: { notion: Notion; matiereId: string }) {
  const [ouvert, setOuvert] = useState(false);
  const cartes = useCartes(notion.id, ouvert);
  const creerCarte = useCreerCarte(matiereId, notion.id);
  const supprimerCarte = useSupprimerCarte(matiereId, notion.id);
  const supprimerNotion = useSupprimerNotion(matiereId);
  const [question, setQuestion] = useState('');
  const [reponse, setReponse] = useState('');

  return (
    <div style={{ borderTop: '1px solid var(--bord)', padding: '0.4rem 0' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <button onClick={() => setOuvert((o) => !o)}>{ouvert ? '▾' : '▸'}</button>
        <span style={{ flex: 1, fontWeight: 600 }}>{notion.intitule}</span>
        {notion.nbCartes === 0 ? (
          <span title="Aucune carte : trou de révision">🕳️ 0 carte</span>
        ) : (
          <span className="attenue">{notion.nbCartes} carte{notion.nbCartes > 1 ? 's' : ''}</span>
        )}
        <button
          className="danger"
          onClick={() => {
            if (window.confirm(`Supprimer la notion « ${notion.intitule} » et ses cartes ?`))
              supprimerNotion.mutate(notion.id);
          }}
        >
          Suppr.
        </button>
      </div>

      {ouvert && (
        <div style={{ paddingLeft: '2rem', marginTop: '0.4rem' }}>
          {cartes.data?.map((c) => (
            <div key={c.id} style={{ display: 'flex', gap: '0.5rem', padding: '0.15rem 0' }}>
              <span style={{ flex: 1 }}>{c.question}</span>
              <button className="danger" onClick={() => supprimerCarte.mutate(c.id)}>
                ×
              </button>
            </div>
          ))}
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.4rem', flexWrap: 'wrap' }}>
            <input placeholder="Question" value={question} onChange={(e) => setQuestion(e.target.value)} style={{ flex: 1, minWidth: 140 }} />
            <input placeholder="Réponse" value={reponse} onChange={(e) => setReponse(e.target.value)} style={{ flex: 1, minWidth: 140 }} />
            <button
              className="primaire"
              disabled={!question.trim() || !reponse.trim() || creerCarte.isPending}
              onClick={() =>
                creerCarte.mutate(
                  { question: question.trim(), reponse: reponse.trim() },
                  {
                    onSuccess: () => {
                      setQuestion('');
                      setReponse('');
                    },
                  },
                )
              }
            >
              + Carte
            </button>
          </div>
        </div>
      )}
    </div>
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
