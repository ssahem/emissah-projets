-- =============================================================================
-- V1 — Baseline : schéma source de la gestion locative
-- Reproduit exactement les 13 tables, les 2 vues et les contraintes nommées de
-- l'export PostgreSQL. Sur une base déjà existante, Flyway marque V1 comme
-- appliquée via baseline-version=1 : ce script ne s'exécute que sur base vierge.
-- Ne jamais modifier ce fichier une fois appliqué (E-07, E-09).
-- =============================================================================

-- Contrainte d'exclusion de F11, absente de l'export car la requête d'export ne
-- sélectionne que les contraintes UNIQUE et CHECK (E-09).
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- -----------------------------------------------------------------------------
-- Propriétaires, immeubles, appartements, pièces, locataires
-- -----------------------------------------------------------------------------

CREATE TABLE proprietaire (
    id_proprietaire INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    telephone       VARCHAR(20),
    CONSTRAINT proprietaire_email_key UNIQUE (email)
);

CREATE TABLE immeuble (
    id_immeuble INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    adresse     VARCHAR(255) NOT NULL,
    code_postal VARCHAR(10)  NOT NULL,
    ville       VARCHAR(100) NOT NULL
);

CREATE TABLE appartement (
    id_appartement  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero          VARCHAR(10)  NOT NULL,
    surface         NUMERIC(7,2) NOT NULL,
    nb_pieces       INTEGER      NOT NULL,
    etage           INTEGER      NOT NULL,
    id_proprietaire INTEGER      NOT NULL,
    id_immeuble     INTEGER      NOT NULL,
    CONSTRAINT appartement_nb_pieces_check CHECK (nb_pieces > 0),
    CONSTRAINT appartement_surface_check CHECK (surface > 0),
    CONSTRAINT uq_appartement_immeuble_numero UNIQUE (id_immeuble, numero),
    CONSTRAINT appartement_id_proprietaire_fkey
        FOREIGN KEY (id_proprietaire) REFERENCES proprietaire(id_proprietaire),
    CONSTRAINT appartement_id_immeuble_fkey
        FOREIGN KEY (id_immeuble) REFERENCES immeuble(id_immeuble)
);

CREATE TABLE piece (
    id_piece INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    libelle  VARCHAR(50) NOT NULL,
    CONSTRAINT uq_piece_libelle UNIQUE (libelle)
);

CREATE TABLE locataire (
    id_locataire INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom          VARCHAR(100) NOT NULL,
    prenom       VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    telephone    VARCHAR(20),
    CONSTRAINT locataire_email_key UNIQUE (email)
);

-- -----------------------------------------------------------------------------
-- Baux et table de liaison (clé primaire composite : E-08)
-- -----------------------------------------------------------------------------

CREATE TABLE bail (
    id_bail           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_debut        DATE         NOT NULL,
    date_fin          DATE,
    provision_charges NUMERIC(12,2) NOT NULL,
    depot_garantie    NUMERIC(12,2) NOT NULL,
    id_appartement    INTEGER      NOT NULL,
    loyer_mensuel     NUMERIC(12,2) NOT NULL,
    CONSTRAINT bail_depot_garantie_check CHECK (depot_garantie >= 0),
    CONSTRAINT bail_provision_charges_check CHECK (provision_charges >= 0),
    CONSTRAINT ck_bail_dates CHECK (date_fin IS NULL OR date_fin >= date_debut),
    CONSTRAINT ck_bail_loyer CHECK (loyer_mensuel > 0),
    CONSTRAINT bail_id_appartement_fkey
        FOREIGN KEY (id_appartement) REFERENCES appartement(id_appartement),
    CONSTRAINT ex_bail_pas_de_chevauchement EXCLUDE USING gist (
        id_appartement WITH =,
        daterange(date_debut, date_fin, '[]') WITH &&
    )
);

CREATE TABLE bail_locataire (
    id_bail      INTEGER NOT NULL,
    id_locataire INTEGER NOT NULL,
    CONSTRAINT bail_locataire_pkey PRIMARY KEY (id_bail, id_locataire),
    CONSTRAINT bail_locataire_id_bail_fkey
        FOREIGN KEY (id_bail) REFERENCES bail(id_bail),
    CONSTRAINT bail_locataire_id_locataire_fkey
        FOREIGN KEY (id_locataire) REFERENCES locataire(id_locataire)
);

-- -----------------------------------------------------------------------------
-- Quittances, paiements, charges, régularisations, états des lieux, constats
-- -----------------------------------------------------------------------------

CREATE TABLE quittance (
    id_quittance            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mois                    DATE         NOT NULL,
    loyer_applique          NUMERIC(12,2) NOT NULL,
    provision_appliquee     NUMERIC(12,2) NOT NULL,
    date_emission           DATE         DEFAULT CURRENT_DATE NOT NULL,
    date_echeance           DATE         NOT NULL,
    id_bail                 INTEGER      NOT NULL,
    regularisation_appliquee NUMERIC(12,2) DEFAULT 0 NOT NULL,
    nature                  VARCHAR(20)  DEFAULT 'LOYER' NOT NULL,
    caution_appliquee       NUMERIC(12,2) DEFAULT 0 NOT NULL,
    CONSTRAINT ck_quittance_echeance CHECK (date_echeance >= date_emission),
    CONSTRAINT ck_quittance_mois CHECK (EXTRACT(DAY FROM mois) = 1),
    CONSTRAINT ck_quittance_nature
        CHECK (nature IN ('LOYER', 'CAUTION', 'REGULARISATION')),
    CONSTRAINT quittance_loyer_applique_check CHECK (loyer_applique >= 0),
    CONSTRAINT quittance_provision_appliquee_check CHECK (provision_appliquee >= 0),
    CONSTRAINT uq_quittance_bail_mois UNIQUE (id_bail, mois),
    CONSTRAINT quittance_id_bail_fkey
        FOREIGN KEY (id_bail) REFERENCES bail(id_bail)
);

CREATE TABLE paiement (
    id_paiement    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_paiement  DATE          DEFAULT CURRENT_DATE NOT NULL,
    montant        NUMERIC(12,2) NOT NULL,
    mode           VARCHAR(20)   NOT NULL,
    id_quittance   INTEGER       NOT NULL,
    CONSTRAINT ck_paiement_montant CHECK (montant > 0),
    CONSTRAINT paiement_montant_check CHECK (montant > 0),
    CONSTRAINT paiement_mode_check
        CHECK (mode IN ('VIREMENT', 'CHEQUE', 'PRELEVEMENT', 'ESPECES', 'CARTE')),
    CONSTRAINT paiement_id_quittance_fkey
        FOREIGN KEY (id_quittance) REFERENCES quittance(id_quittance)
);

CREATE TABLE charge_immeuble (
    id_charge    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annee        INTEGER       NOT NULL,
    nature       VARCHAR(50)   NOT NULL,
    montant      NUMERIC(10,2) NOT NULL,
    id_immeuble  INTEGER       NOT NULL,
    CONSTRAINT charge_immeuble_annee_check CHECK (annee BETWEEN 2000 AND 2100),
    CONSTRAINT charge_immeuble_montant_check CHECK (montant >= 0),
    CONSTRAINT charge_immeuble_id_immeuble_fkey
        FOREIGN KEY (id_immeuble) REFERENCES immeuble(id_immeuble)
);

CREATE TABLE regularisation (
    id_regularisation  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annee              INTEGER       NOT NULL,
    charges_reelles    NUMERIC(10,2) NOT NULL,
    provisions_versees NUMERIC(10,2) NOT NULL,
    solde              NUMERIC(10,2) NOT NULL,
    id_bail            INTEGER       NOT NULL,
    CONSTRAINT regularisation_annee_check CHECK (annee BETWEEN 2000 AND 2100),
    CONSTRAINT regularisation_charges_reelles_check CHECK (charges_reelles >= 0),
    CONSTRAINT regularisation_provisions_versees_check CHECK (provisions_versees >= 0),
    CONSTRAINT uq_regularisation_bail_annee UNIQUE (id_bail, annee),
    CONSTRAINT regularisation_id_bail_fkey
        FOREIGN KEY (id_bail) REFERENCES bail(id_bail)
);

CREATE TABLE etat_lieux (
    id_etat    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type       VARCHAR(10) NOT NULL,
    date_etat  DATE        NOT NULL,
    id_bail    INTEGER     NOT NULL,
    CONSTRAINT etat_lieux_type_check CHECK (type IN ('ENTREE', 'SORTIE')),
    CONSTRAINT uq_etat_lieux_bail_type UNIQUE (id_bail, type),
    CONSTRAINT etat_lieux_id_bail_fkey
        FOREIGN KEY (id_bail) REFERENCES bail(id_bail)
);

CREATE TABLE constat (
    id_etat     INTEGER        NOT NULL,
    id_piece    INTEGER        NOT NULL,
    etat        VARCHAR(10)    NOT NULL,
    commentaire TEXT,
    cout_degat  NUMERIC(12,2)  DEFAULT 0 NOT NULL,
    CONSTRAINT constat_pkey PRIMARY KEY (id_etat, id_piece),
    CONSTRAINT constat_cout_degat_check CHECK (cout_degat >= 0),
    CONSTRAINT constat_etat_check CHECK (etat IN ('BON', 'USE', 'DEGRADE')),
    CONSTRAINT constat_id_etat_fkey
        FOREIGN KEY (id_etat) REFERENCES etat_lieux(id_etat),
    CONSTRAINT constat_id_piece_fkey
        FOREIGN KEY (id_piece) REFERENCES piece(id_piece)
);

-- -----------------------------------------------------------------------------
-- Vues exploitables en lecture seule (aucune entité JPA)
-- -----------------------------------------------------------------------------

CREATE VIEW v_solde_quittance AS
SELECT q.id_quittance,
       q.id_bail,
       q.mois,
       q.nature,
       q.loyer_applique + q.provision_appliquee
           + q.regularisation_appliquee + q.caution_appliquee AS total_du,
       COALESCE(SUM(p.montant), 0) AS total_paye,
       q.loyer_applique + q.provision_appliquee
           + q.regularisation_appliquee + q.caution_appliquee
           - COALESCE(SUM(p.montant), 0) AS reste_a_payer
FROM quittance q
LEFT JOIN paiement p ON p.id_quittance = q.id_quittance
GROUP BY q.id_quittance;

CREATE VIEW v_restitution_depot AS
SELECT b.id_bail,
       b.depot_garantie,
       COALESCE(SUM(c.cout_degat), 0) AS total_degats,
       GREATEST(b.depot_garantie - COALESCE(SUM(c.cout_degat), 0), 0) AS montant_a_restituer
FROM bail b
LEFT JOIN etat_lieux e ON e.id_bail = b.id_bail AND UPPER(e.type) = 'SORTIE'
LEFT JOIN constat c ON c.id_etat = e.id_etat
GROUP BY b.id_bail, b.depot_garantie;
