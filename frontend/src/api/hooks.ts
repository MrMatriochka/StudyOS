import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';
import type {
  Carte,
  Document,
  Echeance,
  EtatEcheance,
  Matiere,
  MajMatiere,
  Notion,
  ResultatDepot,
  ResultatImport,
  ResultatRecherche,
  ScoreMaitrise,
  Seance,
  SessionCarte,
  Section,
} from './types';

// --- Matieres / qualification ---

export function useMatieresAQualifier() {
  return useQuery({
    queryKey: ['matieres', 'a-qualifier'],
    queryFn: () => api.get<Matiere[]>('/api/matieres/a-qualifier'),
  });
}

export function useMatieres() {
  return useQuery({
    queryKey: ['matieres'],
    queryFn: () => api.get<Matiere[]>('/api/matieres'),
  });
}

export function useMatiere(id: string) {
  return useQuery({
    queryKey: ['matieres', id],
    queryFn: () => api.get<Matiere>(`/api/matieres/${id}`),
  });
}

export function useSeancesDeMatiere(id: string) {
  return useQuery({
    queryKey: ['matieres', id, 'seances'],
    queryFn: () => api.get<Seance[]>(`/api/matieres/${id}/seances`),
  });
}

export function useRenommerMatiere() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, maj }: { id: string; maj: MajMatiere }) =>
      api.put<Matiere>(`/api/matieres/${id}`, maj),
    onSuccess: () => invaliderMatieres(qc),
  });
}

export function useFusionner() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ garderId, absorbeeId }: { garderId: string; absorbeeId: string }) =>
      api.post<Matiere>(`/api/matieres/${garderId}/fusionner/${absorbeeId}`),
    onSuccess: () => invaliderMatieres(qc),
  });
}

export function useSupprimerMatiere() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.supprimer<void>(`/api/matieres/${id}`),
    onSuccess: () => {
      invaliderMatieres(qc);
      qc.invalidateQueries({ queryKey: ['seances'] });
      qc.invalidateQueries({ queryKey: ['echeances'] });
    },
  });
}

// --- Seances ---

export function useSeances(du: string, au: string) {
  return useQuery({
    queryKey: ['seances', du, au],
    queryFn: () => api.get<Seance[]>(`/api/seances?du=${du}&au=${au}`),
  });
}

export function useProchaineSeance() {
  return useQuery({
    queryKey: ['seances', 'prochaine'],
    queryFn: () => api.get<Seance | null>('/api/seances/prochaine'),
  });
}

// --- Echeances ---

export function useEcheances(apres: string) {
  return useQuery({
    queryKey: ['echeances', apres],
    queryFn: () => api.get<Echeance[]>(`/api/echeances?apres=${apres}`),
  });
}

export function usePatchEtatEcheance() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, etat }: { id: string; etat: EtatEcheance }) =>
      api.patch<Echeance>(`/api/echeances/${id}`, { etat }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['echeances'] }),
  });
}

// --- Import ---

export function useImporterIcs() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (fichier: File) =>
      api.postFichier<ResultatImport>('/api/imports', 'fichier', fichier),
    onSuccess: () => {
      invaliderMatieres(qc);
      qc.invalidateQueries({ queryKey: ['seances'] });
      qc.invalidateQueries({ queryKey: ['echeances'] });
    },
  });
}

function invaliderMatieres(qc: ReturnType<typeof useQueryClient>) {
  qc.invalidateQueries({ queryKey: ['matieres'] });
}

// --- Documents ---

export function useDocuments(matiereId: string) {
  return useQuery({
    queryKey: ['documents', matiereId],
    queryFn: () => api.get<Document[]>(`/api/documents?matiereId=${matiereId}`),
  });
}

export function useSectionsDocument(documentId: string, actif: boolean) {
  return useQuery({
    queryKey: ['documents', documentId, 'sections'],
    queryFn: () => api.get<Section[]>(`/api/documents/${documentId}/sections`),
    enabled: actif,
  });
}

export function usePropositionSeance(matiereId: string) {
  return useQuery({
    queryKey: ['documents', 'proposition', matiereId],
    queryFn: () =>
      api.get<Seance | null>(`/api/documents/proposition-seance?matiereId=${matiereId}`),
  });
}

export function useDeposerDocument(matiereId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ fichier, seanceId, titre }: { fichier: File; seanceId?: string; titre?: string }) => {
      const form = new FormData();
      form.append('fichier', fichier);
      form.append('matiereId', matiereId);
      if (seanceId) form.append('seanceId', seanceId);
      if (titre) form.append('titre', titre);
      return api.postForm<ResultatDepot>('/api/documents', form);
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['documents', matiereId] }),
  });
}

export function useSupprimerDocument(matiereId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.supprimer<void>(`/api/documents/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['documents', matiereId] }),
  });
}

// --- Recherche ---

export function useRecherche(q: string) {
  return useQuery({
    queryKey: ['recherche', q],
    queryFn: () => api.get<ResultatRecherche[]>(`/api/recherche?q=${encodeURIComponent(q)}`),
    enabled: q.trim().length > 0,
  });
}

// --- Notions & cartes ---

export function useNotions(matiereId: string) {
  return useQuery({
    queryKey: ['notions', matiereId],
    queryFn: () => api.get<Notion[]>(`/api/matieres/${matiereId}/notions`),
  });
}

export function useCreerNotion(matiereId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (intitule: string) =>
      api.post<Notion>('/api/notions', { matiereId, intitule }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notions', matiereId] }),
  });
}

export function useSupprimerNotion(matiereId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.supprimer<void>(`/api/notions/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notions', matiereId] }),
  });
}

export function useCartes(notionId: string, actif: boolean) {
  return useQuery({
    queryKey: ['cartes', notionId],
    queryFn: () => api.get<Carte[]>(`/api/notions/${notionId}/cartes`),
    enabled: actif,
  });
}

export function useCreerCarte(matiereId: string, notionId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ question, reponse }: { question: string; reponse: string }) =>
      api.post<Carte>('/api/cartes', { notionId, question, reponse }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cartes', notionId] });
      qc.invalidateQueries({ queryKey: ['notions', matiereId] }); // met a jour nbCartes
    },
  });
}

export function useSupprimerCarte(matiereId: string, notionId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.supprimer<void>(`/api/cartes/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cartes', notionId] });
      qc.invalidateQueries({ queryKey: ['notions', matiereId] });
    },
  });
}

// --- Revision & maitrise ---

export function useSession(matiereId?: string) {
  const suffixe = matiereId ? `?matiereId=${matiereId}` : '';
  return useQuery({
    queryKey: ['session', matiereId ?? 'toutes'],
    queryFn: () => api.get<SessionCarte[]>(`/api/revisions/session${suffixe}`),
    staleTime: Infinity, // fige la file pendant la session
  });
}

export function useEnregistrerRevision() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ carteId, qualite, dureeMs }: { carteId: string; qualite: number; dureeMs?: number }) =>
      api.post<Carte>('/api/revisions', { carteId, qualite, dureeMs }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['maitrise'] }),
  });
}

export function useMaitrise(matiereId: string) {
  return useQuery({
    queryKey: ['maitrise', matiereId],
    queryFn: () => api.get<ScoreMaitrise>(`/api/matieres/${matiereId}/maitrise`),
  });
}
