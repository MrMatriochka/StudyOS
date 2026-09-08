// Types miroir des DTO du back (records Java). Les instants sont des chaines ISO-8601.

export interface MatiereResume {
  id: string;
  libelle: string;
  couleur: string | null;
  aQualifier: boolean;
}

export interface Matiere {
  id: string;
  code: string | null;
  libelle: string;
  semestre: string | null;
  coefficient: number | null;
  couleur: string | null;
  dateExamen: string | null;
  aQualifier: boolean;
}

export interface Seance {
  id: string;
  libelleBrut: string;
  debut: string;
  fin: string;
  journeeEntiere: boolean;
  salle: string | null;
  type: string | null;
  annulee: boolean;
  matiere: MatiereResume | null;
}

export type EtatEcheance = 'A_FAIRE' | 'EN_COURS' | 'FAIT';

export interface Echeance {
  id: string;
  libelle: string;
  echeance: string;
  etat: EtatEcheance;
  matiere: MatiereResume | null;
}

export interface ResultatImport {
  dejaImporte: boolean;
  crees: number;
  modifies: number;
  inchanges: number;
  annules: number;
}

export interface MajMatiere {
  libelle: string;
  code?: string | null;
  semestre?: string | null;
  coefficient?: number | null;
  couleur?: string | null;
  dateExamen?: string | null;
}

export interface Document {
  id: string;
  titre: string;
  nomOriginal: string;
  type: string;
  extension: string;
  tailleOctets: number;
  nbPages: number | null;
  extractionOk: boolean;
  extractionErr: string | null;
  ajouteLe: string;
  matiere: MatiereResume | null;
  seanceId: string | null;
  seanceDebut: string | null;
}

export interface Section {
  id: string;
  ordre: number;
  granularite: string;
  titre: string | null;
  texte: string;
  notes: string | null;
  page: number | null;
}

export interface ResultatDepot {
  document: Document;
  dejaPresent: boolean;
}

export interface ResultatRecherche {
  documentId: string;
  titre: string;
  extrait: string;
  rang: number;
}

export interface Notion {
  id: string;
  matiereId: string;
  intitule: string;
  description: string | null;
  creeeLe: string;
  nbCartes: number;
}

export interface Carte {
  id: string;
  notionId: string;
  question: string;
  reponse: string;
  origine: string;
  statut: string;
  repetitions: number;
  intervalleJours: number;
  facilite: number;
  derniereRevision: string | null;
  prochaineEcheance: string | null;
  creeeLe: string;
}

export interface SessionCarte {
  id: string;
  question: string;
  reponse: string;
  prevuEncore: number;
  prevuDifficile: number;
  prevuBien: number;
  prevuFacile: number;
}

export interface ScoreMaitrise {
  evaluee: boolean;
  maitrise: number | null;
  couverture: number;
  retention: number;
}
