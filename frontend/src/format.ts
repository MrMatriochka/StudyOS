// Conversion UTC -> Europe/Paris a l'affichage uniquement (brief).

const FUSEAU = 'Europe/Paris';

export function heure(iso: string): string {
  return new Intl.DateTimeFormat('fr-FR', {
    timeZone: FUSEAU,
    hour: '2-digit',
    minute: '2-digit',
  })
    .format(new Date(iso))
    .replace(':', 'h');
}

export function jour(iso: string): string {
  return new Intl.DateTimeFormat('fr-FR', {
    timeZone: FUSEAU,
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  }).format(new Date(iso));
}

export function jourEtHeure(iso: string): string {
  return `${jour(iso)} ${heure(iso)}`;
}

/** Distance a maintenant, en clair : « dans 25 min », « dans 3 h », « dans 4 j ». */
export function tempsRestant(iso: string): string {
  const diffMs = new Date(iso).getTime() - Date.now();
  if (diffMs <= 0) return 'maintenant';
  const min = Math.round(diffMs / 60000);
  if (min < 60) return `dans ${min} min`;
  const h = Math.floor(min / 60);
  if (h < 24) return `dans ${h} h`;
  return `dans ${Math.floor(h / 24)} j`;
}
