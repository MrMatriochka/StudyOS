-- Documents de cours rattaches aux matieres/seances (lot 2).
-- Fichiers sur disque (chemin + hash en base), jamais de BLOB.

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
    -- tsvector recalcule par Postgres a chaque ecriture : pas de trigger a maintenir.
    -- config 'french' = stemming francais + mots vides retires.
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
