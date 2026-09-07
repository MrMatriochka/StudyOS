# StudyOS — brief projet (lots 0 et 1)

Document de cadrage à fournir à Claude Code au démarrage du projet.

---

## 0. Contexte et mode de collaboration

Je suis étudiant en Master 1 MIAGE. Je construis une application web personnelle de suivi
de mon travail : agenda de cours importé depuis un `.ics`, documents rattachés aux matières,
fiches de révision, et un tableau de bord d'accueil.

**Attentes vis-à-vis de toi (Claude Code) :**

- Explique-moi tes choix techniques au fur et à mesure, en français. Je veux progresser,
  pas seulement obtenir du code qui marche.
- Valide l'approche avec moi **avant** d'implémenter un morceau conséquent. Propose,
  attends mon accord, puis code.
- Avance par petits incréments compilables et testables. Pas de gros dump de 15 fichiers.
- Quand tu hésites entre deux conceptions, dis-le et présente le compromis plutôt que de
  trancher silencieusement.
- Si tu constates que les données réelles contredisent une hypothèse de ce document,
  signale-le immédiatement plutôt que de contourner.

---

## 1. Stack imposée

| Élément | Choix | Raison |
|---|---|---|
| Back-end | Spring Boot 3.5, Java 21 (LTS), Maven | Déjà maîtrisé sur un projet précédent |
| Base | PostgreSQL 16 via Docker Compose | JSON natif, recherche plein texte, `pgvector` plus tard |
| Migrations | Flyway, `spring.jpa.hibernate.ddl-auto=validate` | Schéma versionné, jamais généré |
| Parsing ICS | `biweekly` (net.sf.biweekly) | Gère RRULE / EXDATE / RECURRENCE-ID |
| Front | React 19 + Vite + TypeScript | Cohérent avec l'existant |
| État serveur front | TanStack Query | Cache, retry, états de chargement gratuits |
| IDE | IntelliJ IDEA | — |

**Contraintes de contexte :** application **mono-utilisateur, en local sur mon PC**.
Donc **pas d'authentification, pas de JWT, pas de Spring Security** dans ces lots.
CORS ouvert sur `http://localhost:5173` uniquement.

Fichiers documents stockés sur le disque (`~/StudyOS/documents/`), chemin + hash en base.
Jamais de BLOB en base.

---

## 2. Analyse du fichier ICS réel (déjà faite — ne pas refaire)

Fichier source : export Google Calendar d'un agenda partagé de promo
(`X-WR-CALNAME: Cours MIAGE`), **pas un flux ADE**. Il est tenu à la main par des étudiants.

### Chiffres

- 248 `VEVENT`, mais seulement **235 UID distincts**
- **95 libellés `SUMMARY` distincts** pour environ 25 matières réelles
- 24 `RRULE` (hebdomadaires, avec `COUNT` ou `UNTIL`), 11 `EXDATE`, 13 `RECURRENCE-ID`
- `LOCATION` absent sur 53 événements ; 57 valeurs distinctes de salle
- Plage : novembre 2025 → septembre 2026
- 1 seul `DESCRIPTION` sur 248 → champ inutilisable
- Aucun `ATTENDEE`, aucun `ORGANIZER` exploitable → **l'enseignant n'est pas dans le fichier**

### Formats de dates : trois cas à gérer

| Forme | Occurrences | Traitement |
|---|---|---|
| `DTSTART:20260313T133000Z` | 203 | UTC, conversion directe en `Instant` |
| `DTSTART;TZID=Europe/Paris:20260615T133000` | 37 | Heure locale à résoudre avec la `VTIMEZONE` |
| `DTSTART;VALUE=DATE:20260504` | 10 | Journée entière (`Semaine d'exam`, `Vacances - Révision`) |

Stockage en `timestamptz` / `Instant`. Conversion vers `Europe/Paris` **uniquement à
l'affichage**. Un booléen `journee_entiere` sur la séance pour le troisième cas.

### Piège n°1 — la clé naturelle

Les occurrences modifiées d'une série (`RECURRENCE-ID`) **réutilisent l'UID du maître**.
Exemple réel : un cours d'IA déplacé du 13/03 13h30 au 13/03 15h45 porte le même UID que
la série complète.

Donc `UID` seul n'est **pas** une clé unique.

**Clé naturelle retenue : `(uid_externe, debut)`** après développement des occurrences.

### Piège n°2 — la récurrence

Il faut développer les séries, ce qui implique dans l'ordre :

1. Développer la `RRULE` du VEVENT maître (`biweekly` : `DateIterator` sur le composant).
2. Retirer les dates listées en `EXDATE`.
3. Pour chaque VEVENT porteur d'un `RECURRENCE-ID`, **écraser** l'occurrence correspondante
   (l'heure de début peut avoir changé, comme dans l'exemple ci-dessus : on identifie
   l'occurrence à remplacer par la valeur du `RECURRENCE-ID`, pas par son nouveau `DTSTART`).

Exemple réel à utiliser comme cas de test :

```
DTSTART;TZID=Europe/Paris:20260615T133000
RRULE:FREQ=WEEKLY;WKST=MO;COUNT=2;BYDAY=MO
EXDATE;TZID=Europe/Paris:20260622T133000
SUMMARY:Anglais
```

→ doit produire **une seule** séance (le 15/06), pas deux.

### Piège n°3 — les libellés sont sales

Échantillon authentique de variantes désignant la même matière :

- `Qualité SI - CM` · `Qualité SI -CM` · `Qualité SI - TD` · `Examen Qualité des SI` · `Soutenance Qualité SI`
- `Process Metier` · `Process metier` · `Process Métier` · `Processus métier` · `Process Metiers`
- `Entrepot des donnée` · `Entrepot de donnée`
- `App Repartie - TD` · `App Répartie - TD` · `App Repartie - TD ` (espace final) · `Examen App Repartie`
- `Opti Linéaire - TP` · `Opti Linéaire - Tp` · `TP opti` · `Examen  Opti Linéaire` (double espace)
- `Audit` · `AUDIT` · `Audit - Examen`
- `urbanisation` · `Urbanisation des SI` · `Urba des SI`
- `Anglais` · `Anglais ` · `Anglais -` · `Anglais - TD` · `Anglais - Oral Exam` · `Anglais - Exam Oral James`

Salles tout aussi irrégulières : `U4-100` / `u4-100` / `U4 - 100` ; `1TP1 B08` / `1TP1-B08` /
`1TP1 - B08` ; et une coquille `U4-3O1` (lettre O majuscule au lieu du chiffre 0).

**Conclusion de conception : ne pas écrire de regex de reconnaissance de matière.**
Passer par une table d'alias alimentée semi-manuellement (voir §5).

### Piège n°4 — tous les événements ne sont pas des cours

Trois natures cohabitent dans le même agenda :

- **Séances de cours** : `IA`, `NoSQL`, `BD - CM`, `Conception SI - TP`…
- **Échéances** : `Rendu TP Socket /GRPC`, `Rendu Dossier CSI`, `Rendu TP2 Test Unitaire`,
  `App d'entreprise - remise projet`, `Rendu dossier Pilotage pj`
- **Repères de calendrier** (souvent journée entière) : `Semaine d'exam ?`, `Semain d'exam`,
  `Semaine Examen`, `Vacances - Révision`, `Semaine des JNM`, `Conference`, `conf management`

L'import doit **router** : une échéance détectée alimente la table `echeance`, pas `seance`.

---

## 3. Modèle de données — lots 0 et 1

Migration `V1__socle_agenda.sql` :

```sql
create table matiere (
    id           uuid primary key,
    code         varchar(50),
    libelle      varchar(255) not null,
    semestre     varchar(20),
    coefficient  int,
    couleur      varchar(7),
    date_examen  date,
    a_qualifier  boolean not null default false
);

create table alias_matiere (
    id           uuid primary key,
    matiere_id   uuid not null references matiere(id) on delete cascade,
    libelle_norm varchar(500) not null unique
);

create table import_agenda (
    id            uuid primary key,
    source        varchar(20)  not null,
    nom_fichier   varchar(255),
    hash_fichier  varchar(64),
    execute_le    timestamptz  not null,
    nb_crees      int not null default 0,
    nb_modifies   int not null default 0,
    nb_inchanges  int not null default 0,
    nb_annules    int not null default 0
);

create table seance (
    id              uuid primary key,
    matiere_id      uuid references matiere(id),
    import_id       uuid references import_agenda(id),
    uid_externe     varchar(255) not null,
    libelle_brut    varchar(500) not null,
    debut           timestamptz  not null,
    fin             timestamptz  not null,
    journee_entiere boolean not null default false,
    salle           varchar(100),
    salle_brute     varchar(100),
    type            varchar(20),
    annulee         boolean not null default false,
    constraint uk_seance_occurrence unique (uid_externe, debut)
);

create table echeance (
    id           uuid primary key,
    matiere_id   uuid references matiere(id),
    import_id    uuid references import_agenda(id),
    uid_externe  varchar(255),
    libelle      varchar(500) not null,
    libelle_brut varchar(500),
    echeance     timestamptz not null,
    etat         varchar(20) not null default 'A_FAIRE',
    constraint uk_echeance_occurrence unique (uid_externe, echeance)
);

create index idx_seance_debut   on seance(debut);
create index idx_seance_matiere on seance(matiere_id, debut);
create index idx_echeance_date  on echeance(echeance);
```

### Décisions à respecter

- **`matiere_id` est nullable sur `seance`** : une séance dont le libellé n'est rattaché à
  aucune matière doit pouvoir exister, en attente de qualification.
- **`libelle_brut` et `salle_brute` sont conservés systématiquement.** Dans une chaîne
  d'ingestion, on ne jette jamais la donnée source : si une règle de normalisation est
  fausse, on doit pouvoir re-normaliser sans ré-importer.
- **Les contraintes d'unicité sont portées par la base**, pas seulement par le code. Le code
  change, les contraintes restent.
- `import_agenda` sert au débogage : quand une séance est fausse à l'écran, on veut remonter
  à l'import fautif.

---

## 4. Architecture des packages

Découpage **par domaine métier**, pas par couche technique.

```
org.example.studyos
├── agenda
│   ├── domaine        Matiere, Seance, AliasMatiere, ImportAgenda, TypeSeance (enum)
│   ├── depot          MatiereRepository, SeanceRepository, AliasMatiereRepository...
│   ├── ingestion      SourceAgenda (interface), SourceIcs, NormalisateurLibelle,
│   │                  NormalisateurSalle, DeveloppeurRecurrence, ResultatImport
│   ├── service        ServiceAgenda, ServiceImport, ServiceQualification
│   └── web            ControleurAgenda, ControleurImport, DTO (records)
├── suivi
│   ├── domaine        Echeance, EtatEcheance
│   ├── depot
│   ├── service        ServiceTableauDeBord
│   └── web
└── commun             configuration, gestion d'erreurs, utilitaires de texte
```

Justification : par couche, ajouter une fonctionnalité oblige à toucher quatre dossiers
éloignés, et le package `service/` devient illisible à 40 classes. Par domaine, une
fonctionnalité correspond à un sous-arbre, et les dépendances entre modules deviennent
visibles — si `suivi` importe massivement `agenda.domaine`, c'est un signal de couplage.

Note : `import` est un mot-clé Java, d'où `ingestion`.

**Règle : les entités JPA ne sortent jamais des contrôleurs.** DTO en `record` Java,
mappeurs écrits à la main dans le package `web`. Pas de MapStruct pour l'instant.

---

## 5. Algorithme d'import — spécification détaillée

`SourceAgenda` est une interface (`ResultatImport importer(InputStream flux)`) avec une
implémentation `SourceIcs`. Cette abstraction permettra d'ajouter d'autres sources plus tard
sans toucher au service métier.

### Étapes

**1. Empreinte.** Calculer le SHA-256 du flux. Si un `import_agenda` existe déjà avec le même
hash, s'arrêter et retourner « inchangé » (économise le travail sur import répété).

**2. Développement des occurrences.** Pour chaque `VEVENT` :
- s'il porte un `RECURRENCE-ID`, le mettre de côté dans une map `(UID, RECURRENCE-ID) → VEVENT`
- sinon, développer sa `RRULE` (ou produire l'unique occurrence s'il n'y en a pas),
  puis retirer les dates présentes en `EXDATE`

Ensuite, appliquer les overrides : pour chaque entrée de la map, remplacer l'occurrence dont
la date d'origine correspond au `RECURRENCE-ID`.

**3. Classification de la nature.** Sur le libellé normalisé :
- contient `rendu`, `remise`, `livrable`, `depot` → **échéance**
- journée entière et contient `semaine`, `vacances`, `jnm` → **repère**, à ignorer en lot 1
- sinon → **séance**

**4. Détection du type de séance.** Chercher dans le libellé normalisé, dans cet ordre de
priorité (le premier qui matche gagne) :
`examen` | `exam` | `cc` | `soutenance` | `oral` → `EXAMEN` ou `SOUTENANCE`, puis
`tp` → `TP`, `td` → `TD`, `cm` → `CM`, sinon `INCONNU`.
Attention : `td/tp` (dans `BD - TD/TP`) doit donner `TD_TP`.

**5. Normalisation du libellé de matière.** Fonction pure, testable isolément :
- retirer le suffixe de type (` - CM`, ` -CM`, ` - TD/TP`, ` - Tp`…)
- retirer les mots de nature (`examen`, `exam`, `soutenance`, `oral`, `cc`, `rendu`, `dossier`)
- passer en minuscules, retirer les accents (`Normalizer.Form.NFD` + suppression des
  diacritiques), compresser les espaces multiples, `trim()`

**6. Résolution de la matière.** Chercher le libellé normalisé dans `alias_matiere`.
Si trouvé → la matière. Sinon → créer une `matiere` avec `a_qualifier = true` et
un `alias_matiere` pointant dessus.

**7. Normalisation de la salle.** Majuscules, suppression des espaces autour du tiret,
puis la correction connue : dans un identifiant de salle, un `O` entouré de chiffres est
un zéro (`U4-3O1` → `U4-301`). Conserver `salle_brute` dans tous les cas.

**8. Upsert idempotent.** Pour chaque occurrence, rechercher par `(uid_externe, debut)` :
- absente → insertion, `nb_crees++`
- présente et identique → `nb_inchanges++`
- présente et différente → mise à jour, `nb_modifies++`

**9. Détection des annulations.** Toute séance future présente en base, rattachée à un import
antérieur, et absente du flux courant → `annulee = true`, `nb_annules++`.
Ne jamais supprimer physiquement.

### Table d'alias initiale

À proposer en `V2__alias_initiaux.sql`, mais **à me soumettre pour validation avant de
l'écrire** : plusieurs libellés sont ambigus et je suis le seul à savoir à quoi ils
correspondent. Notamment : `M&E`, `NPD`, `PRO`, `Pro CGI`, `CDO`, `IOE`, `UXDM`, `AE`,
`Cyril`, `SI`, `SI - CC`, `IL Test`. Ne devine pas — pose-moi la question, ou laisse-les
partir en `a_qualifier`.

---

## 6. API REST du lot 1

```
POST   /api/imports                    (multipart .ics)  → ResultatImport
GET    /api/imports                                      → historique
GET    /api/seances?du=&au=                              → séances sur une plage
GET    /api/seances/prochaine                            → la prochaine séance à venir
GET    /api/matieres                                     → liste
GET    /api/matieres/a-qualifier                         → matières auto-créées
PUT    /api/matieres/{id}                                → renommer, coefficient, couleur
POST   /api/matieres/{id}/fusionner/{autreId}            → fusion, réaffecte les alias
GET    /api/echeances?apres=                             → échéances à venir
PATCH  /api/echeances/{id}                               → changer l'état
```

L'endpoint de fusion est important : la qualification consistera surtout à fusionner des
doublons créés automatiquement (`Process Metier` et `Processus métier`).

---

## 7. Front du lot 1

Trois écrans, pas plus :

1. **Accueil** — carte « prochain cours » (matière, salle, temps restant, en grand),
   liste des séances du jour, échéances des 14 prochains jours.
2. **Semaine** — grille CSS Grid faite à la main (5 colonnes jours, créneaux 8h-20h, blocs
   positionnés en absolu selon l'heure). Ne pas utiliser FullCalendar : trop lourd et
   pénible à styler pour un besoin aussi simple.
3. **Qualification** — liste des matières `a_qualifier` avec renommage et fusion.
   Écran peu joli mais indispensable dès le premier import.

TanStack Query pour tout ce qui vient de l'API. `useState` réservé à l'état purement UI.

---

## 8. Tests attendus

Le parser doit être testé sur des données réelles, pas inventées.

- Placer un extrait anonymisé du `.ics` dans `src/test/resources/agenda/`.
- **Cas obligatoire** : la série `Anglais` du §2 (`COUNT=2` + un `EXDATE`) doit produire
  exactement 1 séance.
- **Cas obligatoire** : l'override `IA` du 13/03 (`RECURRENCE-ID` à 13h30, `DTSTART` à 15h45)
  doit produire une séance à 15h45, et une seule.
- **Cas obligatoire** : importer deux fois le même fichier ne doit créer aucune séance
  supplémentaire (idempotence).
- Tests unitaires purs sur `NormalisateurLibelle` et `NormalisateurSalle` : ce sont des
  fonctions sans dépendance, elles doivent être couvertes sur toutes les variantes listées
  au §2.

---

## 9. Hors périmètre des lots 0 et 1

Ne pas commencer, ne pas préparer d'abstraction « au cas où » pour :

- documents et extraction de texte
- notions, cartes, répétition espacée (SM-2)
- génération de fiches par LLM
- scores de maîtrise, graphiques
- parsing de PDF
- authentification, multi-utilisateur, déploiement

Le lot 1 est terminé quand je peux importer mon `.ics`, qualifier mes matières, voir ma
semaine et savoir quel est mon prochain cours.
