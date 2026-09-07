# StudyOS — mémo projet

App web **perso, mono-utilisateur, en local** de suivi de travail M1 MIAGE : agenda de cours
importé depuis un `.ics`, matières, échéances, tableau de bord. Le brief complet fait foi :
`STUDYOS_BRIEF.md` (lots 0 et 1).

## Mode de collaboration (important)
- Explications **en français**, expliquer les choix techniques (je veux progresser).
- **Proposer et attendre validation** avant tout morceau conséquent.
- Avancer par **petits incréments** compilables/testables, pas de dump de 15 fichiers.
- Signaler les compromis au lieu de trancher en silence.
- Si les données réelles contredisent le brief → le dire tout de suite.

## Stack imposée
Spring Boot 3.5 / Java 21 / Maven · PostgreSQL 16 (Docker Compose) · Flyway (`ddl-auto=validate`) ·
`biweekly` pour l'ICS · React 19 + Vite + TS + TanStack Query · IntelliJ.
**Pas d'auth / JWT / Spring Security.** CORS ouvert sur `http://localhost:5173` seulement.
Documents sur disque (`~/StudyOS/documents/`), chemin + hash en base, jamais de BLOB.

## Architecture
Packages **par domaine** (pas par couche) : `agenda` (domaine/depot/ingestion/service/web),
`suivi`, `commun`. `import` = mot-clé Java → package `ingestion`.
**Les entités JPA ne sortent jamais des contrôleurs** : DTO en `record`, mappeurs manuels, pas de MapStruct.

## Modèle de données (V1__socle_agenda.sql)
Tables : `matiere`, `alias_matiere`, `import_agenda`, `seance`, `echeance`.
- `seance.matiere_id` **nullable** (séance en attente de qualification).
- `libelle_brut` / `salle_brute` **toujours conservés** (re-normaliser sans ré-importer).
- Unicité portée par la **base** : `seance(uid_externe, debut)`, `echeance(uid_externe, echeance)`.
- Dates en `timestamptz`/`Instant`, conversion `Europe/Paris` **à l'affichage seulement**.

## Pièges ICS (données réelles, analyse déjà faite — ne pas refaire)
1. `RECURRENCE-ID` réutilise l'UID du maître → **clé naturelle = `(uid_externe, debut)`**, pas l'UID seul.
2. Récurrence : développer RRULE → retirer EXDATE → écraser les occurrences via RECURRENCE-ID (identifier par la date d'origine, pas le nouveau DTSTART).
3. Libellés sales (95 SUMMARY pour ~25 matières) → **pas de regex**, table `alias_matiere` semi-manuelle.
4. Tout n'est pas un cours → router : **séance** / **échéance** (rendu, remise…) / **repère** (ignoré lot 1).
3 formats de dates : UTC `Z`, `TZID=Europe/Paris`, `VALUE=DATE` (journée entière).

## Import (idempotent)
Hash SHA-256 (skip si déjà importé) → développement occurrences → classification nature →
type séance (EXAMEN/SOUTENANCE > TP > TD > CM > INCONNU, `TD/TP`→`TD_TP`) →
normalisation libellé (fonction pure testable) → résolution matière (sinon créée `a_qualifier=true`) →
normalisation salle (`O` entre chiffres = `0`) → upsert `(uid_externe, debut)` → détection annulations (jamais de suppression physique).
`SourceAgenda` = interface, `SourceIcs` = impl.
**`V2__alias_initiaux.sql` : me soumettre les alias ambigus avant d'écrire** (M&E, NPD, PRO, CDO, IOE…), ne jamais deviner.

## API lot 1
`POST/GET /api/imports` · `GET /api/seances?du=&au=` · `/api/seances/prochaine` ·
`GET/PUT /api/matieres` · `/api/matieres/a-qualifier` · `POST .../fusionner/{autreId}` ·
`GET /api/echeances?apres=` · `PATCH /api/echeances/{id}`. La **fusion** de doublons est centrale.

## Front lot 1 (3 écrans)
Accueil (carte « prochain cours ») · Semaine (CSS Grid maison, **pas de FullCalendar**) · Qualification (renommage/fusion).
TanStack Query pour l'API, `useState` pour l'UI pure.

## Tests obligatoires (données réelles, extrait dans `src/test/resources/agenda/`)
- Série `Anglais` (COUNT=2 + 1 EXDATE) → **1 seule** séance.
- Override `IA` du 13/03 (RECURRENCE-ID 13h30, DTSTART 15h45) → 1 séance à 15h45.
- Double import → **0 séance en plus** (idempotence).
- `NormalisateurLibelle` / `NormalisateurSalle` : couvrir toutes les variantes du brief.

## Hors périmètre (ne pas préparer d'abstraction « au cas où »)
Documents/OCR, notions/cartes/SM-2, fiches LLM, scores de maîtrise, PDF, auth/multi-user/déploiement.

**Lot 1 fini quand** : je peux importer mon `.ics`, qualifier mes matières, voir ma semaine, savoir mon prochain cours.
