-- Socle agenda (lots 0 et 1).
-- Regles : unicite portee par la base, libelles/salles bruts conserves,
-- dates en timestamptz, matiere_id nullable sur seance (qualification differee).

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
