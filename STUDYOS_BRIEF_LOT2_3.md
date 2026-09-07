# StudyOS — brief projet (lots 2 et 3)

Suite de `STUDYOS_BRIEF.md`. Les conventions du §0 (mode de collaboration), du §1 (stack)
et du §4 (architecture des packages) de ce document restent valables et ne sont pas répétées.

**Prérequis : lots 0 et 1 terminés** — import ICS, matières, séances, échéances, vue semaine.

---

## Lot 2 — Documents de cours

### Objectif

Rattacher mes supports (slides, sujets de TP, annales, mes propres notes) aux matières et
aux séances, en extraire le texte, et pouvoir les chercher. Le lot 2 n'a pas de valeur en
soi : il existe pour alimenter le lot 3. C'est pourquoi il doit rester minimal.

### Nouveau package

```
org.example.studyos
└── contenu
    ├── domaine        Document, SectionDocument, TypeDocument, Granularite (enums)
    ├── depot          DocumentRepository, SectionDocumentRepository
    ├── extraction     ExtracteurTexte (interface), ExtracteurTika, ResultatExtraction,
    │                  SectionneurDocument (interface), SectionneurPdf, SectionneurPptx,
    │                  SectionneurDocx, SectionneurParDefaut, FabriqueSectionneur,
    │                  NettoyeurRepetitions
    ├── stockage       DepotFichier
    ├── service        ServiceDocument, ServiceRecherche
    └── web            ControleurDocument, DTO
```

### Migration `V3__documents.sql`

```sql
create table document (
    id             uuid primary key,
    matiere_id     uuid not null references matiere(id) on delete cascade,
    seance_id      uuid references seance(id) on delete set null,
    titre          varchar(500) not null,
    nom_original   varchar(500) not null,
    type           varchar(30)  not null,
    extension      varchar(10)  not null,
    chemin         varchar(500) not null,
    hash           varchar(64)  not null unique,
    taille_octets  bigint       not null,
    nb_pages       int,
    texte_extrait  text,
    extraction_ok  boolean not null default false,
    extraction_err varchar(500),
    ajoute_le      timestamptz not null,
    recherche      tsvector generated always as (
                       to_tsvector('french',
                           coalesce(titre, '') || ' ' || coalesce(texte_extrait, ''))
                   ) stored
);

create table section_document (
    id           uuid primary key,
    document_id  uuid not null references document(id) on delete cascade,
    ordre        int  not null,
    granularite  varchar(20) not null,
    titre        varchar(500),
    texte        text not null,
    notes        text,
    page         int,
    constraint uk_section_ordre unique (document_id, ordre)
);

create index idx_document_matiere   on document(matiere_id);
create index idx_document_recherche on document using gin(recherche);
create index idx_section_document   on section_document(document_id, ordre);
```

### Décisions à respecter

**Le `hash` est unique et c'est volontaire.** Si je redépose le même fichier (ce qui arrivera,
les profs renvoient les mêmes slides), on ne crée pas de doublon : on retourne le document
existant en informant l'utilisateur. La déduplication par empreinte de contenu est plus
fiable que par nom de fichier.

**Le chemin de stockage est dérivé de l'`id`, pas du nom original.**
Forme : `~/StudyOS/documents/{hash[0:2]}/{id}.{extension}`.
Raison : les noms de fichiers fournis par les enseignants sont ingérables (`CM3_v2_FINAL.pdf`,
`cours (1).pdf`, caractères accentués et espaces) et entrent en collision. On conserve
`nom_original` en base pour l'afficher, et on ne s'en sert jamais comme identifiant.
Le sous-répertoire à deux caractères évite d'avoir 3000 fichiers à plat dans un dossier.

**`recherche` est une colonne générée.** Postgres recalcule le `tsvector` automatiquement à
chaque écriture — pas de trigger à maintenir, pas de risque de désynchronisation entre le
texte et son index. Note la configuration `'french'` : elle applique le *stemming* français
(« conception » et « conceptions » deviennent le même lexème) et retire les mots vides.
Avec `'simple'` ou `'english'`, tes recherches en français seraient nettement moins bonnes.

**`extraction_ok` et `extraction_err` plutôt qu'une exception qui remonte.** Une extraction
qui échoue (PDF scanné, fichier corrompu) ne doit pas empêcher l'ajout du document : je veux
quand même pouvoir l'ouvrir. L'échec est une donnée, pas un incident.

### Extraction

Utiliser **Apache Tika** (`tika-core` + `tika-parsers-standard-package`) plutôt que PDFBox et
POI séparément. Justification : une seule façade pour PDF, PPTX, DOCX, ODT et TXT, avec
détection automatique du type. Le prix à payer est une dépendance lourde et moins de contrôle
fin ; c'est le bon compromis ici puisqu'on ne fait que de l'extraction de texte brut.

`ExtracteurTexte` est une interface (`ResultatExtraction extraire(Path fichier)`), avec
`ExtracteurTika` comme implémentation. Même raison que pour `SourceAgenda` au lot 1 :
isoler la dépendance externe derrière une frontière que je contrôle.

**Découpage en sections** — c'est ce qui rendra le lot 3 possible, et mes documents sont un
mélange de PDF, PPTX et divers. Un « découpage » ne veut pas dire la même chose selon le
format, donc ne pas chercher à unifier : prendre l'unité structurelle que chaque format
fournit gratuitement, et la nommer.

`SectionneurDocument` est une interface (`List<Section> decouper(String xmlTika)`) avec une
implémentation par famille de format, choisie par type MIME dans une fabrique. Même motif
que `SourceAgenda` au lot 1.

| Format | Unité | Comment |
|---|---|---|
| PPTX | Diapositive | Tika émet un `<div class="slide-content">` par diapositive avec `ToXMLContentHandler` |
| PDF | Page | Tika émet un `<div class="page">` par page |
| DOCX / ODT | Titre de niveau 1-2 | Découper sur les `<h1>` / `<h2>` du XML Tika ; si aucun, une seule section |
| TXT / MD | Titre Markdown, sinon tout | Ne pas s'acharner |
| Autre | Section unique | `granularite = DOCUMENT_ENTIER` |

Stocker la granularité obtenue sur chaque section. Le lot 3 doit savoir s'il regarde une
diapositive de 40 mots ou un document entier de 20 pages — ce n'est pas la même chose pour
en tirer des notions.

**Notes du présentateur (PPTX).** Tika les expose dans `<div class="slide-notes">`. Elles ont
souvent plus de valeur pédagogique que la diapositive elle-même (l'enseignant y met ce qu'il
dit à l'oral), mais elles sont aussi parfois vides ou parasites. Les stocker dans une colonne
`notes` distincte du texte de la section, jamais concaténées : le lot 4 voudra les pondérer
différemment.

**Détection des PDF scannés.** Un PDF sans couche texte produit une extraction vide ou quasi
vide sans lever d'exception. Règle : si le nombre moyen de caractères par page est inférieur
à 50, considérer l'extraction comme échouée avec le message « pas de couche texte, PDF
probablement scanné » et ne créer aucune section. Sans ce garde-fou, le lot 3 fabriquera des
notions à partir de rien et je ne comprendrai pas pourquoi.

**Nettoyage des répétitions.** Les en-têtes, pieds de page, numéros de diapositive et
mentions de copyright reviennent sur chaque page et polluent chaque section. Heuristique
simple et efficace : après découpage, toute ligne courte (moins de 80 caractères) présente
à l'identique dans plus de 70 % des sections d'un même document est retirée de toutes.
Ne l'appliquer qu'aux documents de plus de 5 sections.

**Deux limites à accepter, pas à combattre.** Les PDF exportés en mode « 4 diapositives par
page » donneront une section pour quatre diapositives : c'est imparfait mais exploitable,
et le détecter de façon fiable coûte plus cher que ça ne rapporte. Les PDF sur deux colonnes
peuvent sortir dans un ordre de lecture incorrect : c'est une limite connue de l'extraction
par flux, on vit avec.

L'extraction est **synchrone** au dépôt. Sur un usage local avec des fichiers de quelques Mo,
l'asynchrone n'apporterait qu'une complexité de gestion d'état (`EN_ATTENTE`, `EN_COURS`…)
pour économiser deux secondes.

### Rattachement à une séance

Au dépôt, **proposer** la séance la plus proche : même matière, écart minimal entre la date
du jour et la date de la séance, dans une fenêtre de ±10 jours. Proposer, pas imposer :
l'affectation est pré-remplie dans le formulaire et je peux la changer ou la vider.

Règle générale : une heuristique alimente un choix par défaut, elle ne décide jamais seule
d'une donnée que l'utilisateur voit ensuite comme un fait.

### API

```
POST   /api/documents                  (multipart : fichier, matiereId, seanceId?, titre?)
GET    /api/documents?matiereId=&seanceId=
GET    /api/documents/{id}
GET    /api/documents/{id}/fichier     → flux binaire, Content-Disposition: inline
GET    /api/documents/{id}/sections
PATCH  /api/documents/{id}             → titre, type, rattachement séance
DELETE /api/documents/{id}             → supprime la ligne ET le fichier
GET    /api/recherche?q=               → recherche plein texte, retourne documents + extraits
```

Pour la recherche, utiliser `ts_rank` pour l'ordre et `ts_headline` pour les extraits
surlignés. C'est une requête native (`@Query(nativeQuery = true)`) — JPQL ne connaît pas
`tsvector`. C'est normal et acceptable : quand on utilise une fonctionnalité spécifique au
SGBD, on assume le SQL natif plutôt que de la contourner.

### Front

- **Page matière** : en-tête (libellé, coefficient, date d'examen), liste des séances,
  liste des documents avec zone de dépôt par glisser-déposer.
- **Barre de recherche globale** dans l'en-tête, résultats avec extraits surlignés.
- Ouverture d'un document dans un nouvel onglet via `/api/documents/{id}/fichier`.
  Pas de visionneuse intégrée.

### Hors périmètre du lot 2

Pas d'OCR sur les PDF scannés. Pas de miniatures. Pas de versionnement de documents. Pas
d'annotation. Pas de dossier surveillé (à envisager plus tard, mais le dépôt manuel suffit
pour valider le besoin).

### Le lot 2 est terminé quand

Je peux déposer les slides d'une UE, elles apparaissent sur la page de la matière rattachées
à la bonne séance, leur texte est extrait et découpé en sections, et une recherche sur un mot
du contenu me les retrouve.

---

## Lot 3 — Notions, cartes et révision espacée

### Objectif

C'est le cœur du projet. Décomposer chaque matière en notions, associer des cartes
question/réponse à ces notions, et les planifier dans le temps avec un algorithme de
répétition espacée.

Sans ce lot, « niveau de connaissance d'une matière » est un chiffre inventé. Avec lui,
c'est une mesure.

### Nouveau package

```
org.example.studyos
└── revision
    ├── domaine        Notion, Carte, Revision, StatutCarte, OrigineCarte, EtatPlanification
    ├── depot          NotionRepository, CarteRepository, RevisionRepository
    ├── planification  PlanificateurRevision (interface), Sm2Planificateur
    ├── service        ServiceNotion, ServiceCarte, ServiceSessionRevision, ServiceMaitrise
    └── web            ControleurNotion, ControleurCarte, ControleurRevision, DTO
```

### Migration `V4__revision.sql`

```sql
create table notion (
    id                  uuid primary key,
    matiere_id          uuid not null references matiere(id) on delete cascade,
    section_document_id uuid references section_document(id) on delete set null,
    intitule            varchar(500) not null,
    description         text,
    creee_le            timestamptz not null
);

create table carte (
    id                 uuid primary key,
    notion_id          uuid not null references notion(id) on delete cascade,
    question           text not null,
    reponse            text not null,
    origine            varchar(20) not null,
    statut             varchar(20) not null default 'VALIDEE',
    -- état de planification (voir note sur la dénormalisation)
    repetitions        int     not null default 0,
    intervalle_jours   int     not null default 0,
    facilite           real    not null default 2.5,
    derniere_revision  date,
    prochaine_echeance date,
    creee_le           timestamptz not null
);

create table revision (
    id               uuid primary key,
    carte_id         uuid not null references carte(id) on delete cascade,
    faite_le         timestamptz not null,
    qualite          int  not null,
    -- état résultant, figé pour pouvoir rejouer l'historique plus tard
    repetitions      int  not null,
    intervalle_jours int  not null,
    facilite         real not null,
    duree_ms         int
);

create index idx_notion_matiere    on notion(matiere_id);
create index idx_carte_notion      on carte(notion_id);
create index idx_carte_echeance    on carte(prochaine_echeance) where statut = 'VALIDEE';
create index idx_revision_carte    on revision(carte_id, faite_le);
```

### La décision de conception à comprendre : dénormalisation assumée

L'état de planification (`repetitions`, `intervalle_jours`, `facilite`, `prochaine_echeance`)
est stocké **à la fois** sur `carte` et sur chaque ligne de `revision`. C'est une duplication
délibérée, et c'est le point le plus intéressant du schéma.

En théorie, l'état courant d'une carte est entièrement reconstructible depuis son historique
de révisions : c'est de l'*event sourcing*. On pourrait ne stocker que le journal.

En pratique, la requête la plus fréquente de l'application est « quelles cartes sont dues
aujourd'hui ». En pur event sourcing, elle demanderait de rejouer l'historique de chaque
carte, ou une fonction fenêtrée pour récupérer la dernière révision de chacune — à chaque
ouverture de l'écran de révision.

Le compromis retenu : **l'état courant sur `carte` pour la lecture** (index partiel sur
`prochaine_echeance`, requête triviale), **le journal complet sur `revision` pour l'audit et
le rejeu**. Le jour où je voudrai passer de SM-2 à FSRS, le journal me permettra de recalculer
tous les états sans avoir perdu d'information.

C'est un cas d'école du compromis normalisation / performance de lecture, et il faut savoir
le justifier — pas le subir.

### L'algorithme SM-2

`PlanificateurRevision` est une interface :

```java
public interface PlanificateurRevision {
    EtatPlanification planifier(EtatPlanification courant, int qualite, LocalDate aujourdhui);
}
```

`EtatPlanification` est un `record` immuable : `(int repetitions, int intervalleJours,
double facilite, LocalDate prochaineEcheance)`.

**Cette fonction doit être pure** : pas d'accès base, pas d'injection Spring, pas d'appel à
`LocalDate.now()` à l'intérieur (la date est un paramètre). Une fonction pure se teste sans
contexte Spring, en millisecondes, sur des dizaines de cas.

Règles de SM-2 :

1. Si `qualite < 3` (échec) : `repetitions = 0`, `intervalleJours = 1`.
2. Sinon, selon `repetitions` : `0 → 1 jour`, `1 → 6 jours`,
   sinon `intervalleJours = round(intervalleJours × facilite)`. Puis `repetitions++`.
3. Dans tous les cas, ajuster la facilité :
   `facilite += 0.1 - (5 - q) × (0.08 + (5 - q) × 0.02)`, **plancher à 1.3**.
   Le plancher est indispensable : sans lui, une carte ratée plusieurs fois verrait son
   intervalle tomber à zéro et réapparaîtrait en boucle infinie.
4. `prochaineEcheance = aujourdhui + intervalleJours`.

`Sm2Planificateur` est la seule implémentation pour l'instant. L'interface n'est pas de la
sur-ingénierie ici : elle a une deuxième implémentation identifiée (FSRS) et elle rend la
fonction remplaçable dans les tests.

### Notation à quatre niveaux

L'échelle SM-2 d'origine va de 0 à 5, mais six choix pour noter son propre rappel est une
charge cognitive inutile — Anki a convergé vers quatre après des années d'usage.

L'interface expose donc quatre boutons, mappés ainsi :

| Bouton | Qualité SM-2 |
|---|---|
| Encore | 0 |
| Difficile | 3 |
| Bien | 4 |
| Facile | 5 |

Le mapping vit dans le contrôleur ou le DTO, **pas dans le planificateur** : l'algorithme
continue de raisonner sur l'échelle 0-5.

### La session de révision

`GET /api/revisions/session` compose la file du jour :

1. Les cartes **dues** : `statut = 'VALIDEE'` et `prochaine_echeance <= aujourd'hui`.
2. Les cartes **nouvelles** : `prochaine_echeance is null`, **plafonnées à 20 par jour**.

Le plafond sur les nouvelles cartes est important et contre-intuitif. Si j'introduis 200
cartes le jour où je crée une matière, SM-2 me les ramènera toutes ensemble à J+1, puis à
J+6, et je me retrouverai avec des journées de révision ingérables trois semaines plus tard.
Le plafond lisse la charge future. Rends-le configurable par matière, avec 20 par défaut.

Ordre de présentation : les cartes dues d'abord, en mélangeant les matières (l'alternance
améliore la rétention par rapport au blocage par thème), puis les nouvelles.

### Le score de maîtrise

Calculé à la demande dans `ServiceMaitrise`, **jamais stocké** à ce stade (l'historisation
viendra au lot 5, elle n'a d'intérêt que pour tracer des courbes).

Pour une matière :

- `couverture` = nombre de notions ayant au moins une carte validée / nombre total de notions
- `retention` = moyenne sur les cartes validées de
  `exp(-joursDepuisDerniereRevision / max(intervalleJours, 1))`
- `maitrise = couverture × retention`, dans `[0, 1]`

Le terme de rétention est une approximation de la *courbe d'oubli* d'Ebbinghaus : la
probabilité de se souvenir décroît exponentiellement avec le temps, et l'intervalle courant
de la carte sert d'estimateur de sa stabilité en mémoire. Une carte révisée hier avec un
intervalle de 30 jours donne une rétention proche de 1 ; la même carte oubliée depuis
60 jours tombe vers 0,13.

Conséquence voulue : **le score décroît tout seul si je ne révise pas.** C'est exactement le
comportement attendu d'un « niveau de connaissance », et c'est ce qui rend le tableau de bord
honnête.

Cas limite à gérer explicitement : une matière sans aucune notion doit retourner `null` et
s'afficher « non évaluée », pas `0`. Zéro voudrait dire « je ne sais rien », alors que la
réalité est « je n'ai rien saisi ».

### API

```
GET    /api/matieres/{id}/notions
POST   /api/notions
PATCH  /api/notions/{id}
DELETE /api/notions/{id}
GET    /api/notions/{id}/cartes
POST   /api/cartes
PATCH  /api/cartes/{id}
DELETE /api/cartes/{id}
GET    /api/revisions/session?matiereId=&limite=
POST   /api/revisions                     { carteId, qualite, dureeMs }  → carte mise à jour
GET    /api/matieres/{id}/maitrise
GET    /api/tableau-de-bord               → agrégat pour l'accueil
```

`POST /api/revisions` retourne la carte avec son nouvel état : le front n'a pas à recalculer
ni à redemander la session.

### Front

**Écran de révision** — c'est celui que j'utiliserai le plus, il mérite le plus de soin :

- Une carte à la fois, la question seule d'abord.
- Espace ou clic révèle la réponse.
- Quatre boutons de notation, avec les raccourcis `1` à `4`.
- Compteur de restantes, et l'intervalle prévu affiché sous chaque bouton
  (« Bien → 6 j ») : c'est ce qui permet de noter avec justesse.
- Aucune navigation parasite pendant une session.

**Écran notions d'une matière** — arborescence notions → cartes, création rapide au clavier,
et un marqueur visible sur les notions sans aucune carte (ce sont les trous de révision).

**Accueil enrichi** — ajouter aux blocs du lot 1 : « N cartes à réviser aujourd'hui » en
appel à l'action principal, et les trois matières au score de maîtrise le plus faible
pondéré par la proximité de leur date d'examen.

### Tests attendus

- `Sm2Planificateur` : une séquence de référence vérifiée pas à pas.
  Depuis l'état initial, avec des notes `4, 4, 4` : les intervalles doivent être
  `1 j`, puis `6 j`, puis `6 × facilite` arrondi. Avec une note `0` en quatrième position,
  retour à `repetitions = 0` et `intervalle = 1 j`.
- Le plancher de facilité : dix échecs consécutifs ne doivent jamais faire descendre
  `facilite` sous `1.3`.
- `ServiceMaitrise` : une matière sans notion retourne `null`, une matière dont toutes les
  cartes ont été révisées aujourd'hui retourne une valeur proche de la couverture.
- Le plafond de nouvelles cartes : avec 100 cartes neuves, la session en contient 20.

### Hors périmètre du lot 3

- **Aucune génération par IA.** Toutes les cartes sont saisies à la main dans ce lot.
  C'est volontaire : je veux valider que l'algorithme et l'ergonomie tiennent la route
  avant d'ajouter une source de cartes automatique. Prévoir simplement le champ `origine`.
- Pas de FSRS. Pas de sous-cartes ou de types de cartes (texte à trous, QCM).
- Pas d'historisation du score de maîtrise, pas de graphiques.
- Pas d'import/export Anki.

### Le lot 3 est terminé quand

Je peux découper une UE en notions, y attacher des cartes, faire une session de révision au
clavier sans souris, et voir sur l'accueil combien de cartes m'attendent et quelle matière
est en train de me filer entre les doigts.
