import { useState } from 'react';
import {
  useFusionner,
  useImporterIcs,
  useMatieres,
  useMatieresAQualifier,
  useRenommerMatiere,
} from '../api/hooks';
import type { Matiere } from '../api/types';

export function Qualification() {
  const aQualifier = useMatieresAQualifier();
  const toutes = useMatieres();
  const importer = useImporterIcs();
  const [fichier, setFichier] = useState<File | null>(null);

  return (
    <div>
      <section className="carte">
        <h2>Importer un agenda (.ics)</h2>
        <input
          type="file"
          accept=".ics"
          onChange={(e) => setFichier(e.target.files?.[0] ?? null)}
        />
        <button
          className="primaire"
          disabled={!fichier || importer.isPending}
          onClick={() => fichier && importer.mutate(fichier)}
          style={{ marginLeft: '0.5rem' }}
        >
          {importer.isPending ? 'Import…' : 'Importer'}
        </button>
        {importer.data && (
          <p className="attenue">
            {importer.data.dejaImporte
              ? 'Fichier déjà importé (inchangé).'
              : `Créés ${importer.data.crees} · modifiés ${importer.data.modifies} · inchangés ${importer.data.inchanges} · annulés ${importer.data.annules}`}
          </p>
        )}
        {importer.isError && <p style={{ color: 'crimson' }}>Échec de l'import.</p>}
      </section>

      <h2>
        Matières à qualifier{' '}
        {aQualifier.data && <span className="attenue">({aQualifier.data.length})</span>}
      </h2>

      {aQualifier.isLoading && <p className="attenue">Chargement…</p>}
      {aQualifier.isError && <p style={{ color: 'crimson' }}>Erreur de chargement.</p>}
      {aQualifier.data?.length === 0 && (
        <p className="attenue">Aucune matière à qualifier. 🎉</p>
      )}

      {aQualifier.data?.map((matiere) => (
        <LigneMatiere
          key={matiere.id}
          matiere={matiere}
          candidates={(toutes.data ?? []).filter((m) => m.id !== matiere.id)}
        />
      ))}
    </div>
  );
}

function LigneMatiere({ matiere, candidates }: { matiere: Matiere; candidates: Matiere[] }) {
  const renommer = useRenommerMatiere();
  const fusionner = useFusionner();
  const [libelle, setLibelle] = useState(matiere.libelle);
  const [cibleId, setCibleId] = useState('');

  return (
    <div className="carte" style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
      <input value={libelle} onChange={(e) => setLibelle(e.target.value)} style={{ flex: '1 1 220px' }} />

      <button
        disabled={renommer.isPending || libelle.trim() === ''}
        onClick={() => renommer.mutate({ id: matiere.id, maj: { libelle: libelle.trim() } })}
      >
        Valider le nom
      </button>

      <span className="attenue">ou fusionner dans</span>

      <select value={cibleId} onChange={(e) => setCibleId(e.target.value)} style={{ flex: '1 1 220px' }}>
        <option value="">— choisir une matière —</option>
        {candidates.map((c) => (
          <option key={c.id} value={c.id}>
            {c.libelle}
          </option>
        ))}
      </select>

      <button
        className="primaire"
        disabled={cibleId === '' || fusionner.isPending}
        onClick={() => fusionner.mutate({ garderId: cibleId, absorbeeId: matiere.id })}
      >
        Fusionner
      </button>
    </div>
  );
}
