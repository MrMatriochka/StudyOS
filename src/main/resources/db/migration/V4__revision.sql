-- Notions, cartes et revision espacee SM-2 (lot 3).
-- Denormalisation assumee : l'etat de planification vit A LA FOIS sur `carte`
-- (etat courant, lecture triviale des cartes dues) et sur chaque `revision`
-- (journal complet, pour l'audit et le rejeu si on passe a FSRS).

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
    -- etat de planification (denormalisation, voir en-tete)
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
    -- etat resultant, fige pour rejouer l'historique plus tard
    repetitions      int  not null,
    intervalle_jours int  not null,
    facilite         real not null,
    duree_ms         int
);

create index idx_notion_matiere on notion(matiere_id);
create index idx_carte_notion   on carte(notion_id);
-- index partiel : seules les cartes validees sont interrogees par « dues aujourd'hui ».
create index idx_carte_echeance on carte(prochaine_echeance) where statut = 'VALIDEE';
create index idx_revision_carte on revision(carte_id, faite_le);
