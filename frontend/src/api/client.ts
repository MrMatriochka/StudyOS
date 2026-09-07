// Base de l'API. Le front (5173) appelle le back (8080) : le CORS du back l'autorise.
const BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080';

export class ErreurApi extends Error {
  constructor(public statut: number, message: string) {
    super(message);
  }
}

async function requete<T>(chemin: string, options?: RequestInit): Promise<T> {
  const reponse = await fetch(`${BASE}${chemin}`, options);
  if (!reponse.ok) {
    throw new ErreurApi(reponse.status, `${reponse.status} sur ${chemin}`);
  }
  // 204 No Content (ex : /seances/prochaine quand aucune) -> pas de corps.
  if (reponse.status === 204) {
    return undefined as T;
  }
  return reponse.json() as Promise<T>;
}

export const api = {
  get: <T>(chemin: string) => requete<T>(chemin),

  put: <T>(chemin: string, corps: unknown) =>
    requete<T>(chemin, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(corps),
    }),

  patch: <T>(chemin: string, corps: unknown) =>
    requete<T>(chemin, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(corps),
    }),

  post: <T>(chemin: string, corps?: unknown) =>
    requete<T>(chemin, {
      method: 'POST',
      headers: corps === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: corps === undefined ? undefined : JSON.stringify(corps),
    }),

  postFichier: <T>(chemin: string, champ: string, fichier: File) => {
    const form = new FormData();
    form.append(champ, fichier);
    return requete<T>(chemin, { method: 'POST', body: form });
  },
};
