import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';
import type {
  Echeance,
  EtatEcheance,
  Matiere,
  MajMatiere,
  ResultatImport,
  Seance,
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
