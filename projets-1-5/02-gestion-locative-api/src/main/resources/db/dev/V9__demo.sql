-- Données de démonstration — profil "dev" uniquement (jamais en production).
-- Numérotées V9 : V1 à V8 sont les migrations du schéma (V8 = date_creation),
-- l'ancien fichier de démo portait à tort la version V8 et empêchait Flyway
-- de démarrer ("Found more than one migration with version 8").
--
-- Jeu volontairement complet : 2 propriétaires, 3 immeubles, 8 appartements
-- (dont 2 jamais loués et 2 libres), 5 locataires, 5 baux (EN_COURS, TERMINE,
-- résilié, renouvelé), des révisions de loyer, des quittances/paiements et un
-- état des lieux de sortie. Il couvre les requêtes F1 à F19 et les deux vues.

-- ════════════════════════════════════════════════════════════════════════════
--  PROPRIÉTAIRES (F1) ET IMMEUBLES (F2, F7)
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO proprietaire (id_proprietaire, nom, prenom, email, telephone)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'Diallo',  'Awa',   'awa.diallo@example.test',   '0102030405'),
    (2, 'Kouassi', 'Jean',  'jean.kouassi@example.test',  '0102030406')
ON CONFLICT (id_proprietaire) DO NOTHING;

INSERT INTO immeuble (id_immeuble, adresse, code_postal, ville, id_proprietaire, nb_etages)
OVERRIDING SYSTEM VALUE VALUES
    (1, '12 rue des Acacias',       '75001', 'Paris',     1, 3),
    (2, '8 avenue de la République', '75002', 'Paris',     1, 2),
    (3, '5 rue du Port',            '13001', 'Marseille', 2, 4)
ON CONFLICT (id_immeuble) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  APPARTEMENTS (F3, F6, F9, F17, F18)
--  La date_creation borne la vacance des logements jamais loués (F18).
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO appartement (id_appartement, numero, surface, nb_pieces, etage,
                         id_immeuble, date_creation)
OVERRIDING SYSTEM VALUE VALUES
    (1, '1A', 45.50, 2, 1, 1, DATE '2025-01-01'),  -- bail 1, EN_COURS
    (2, '1B', 65.00, 3, 1, 1, DATE '2025-01-01'),  -- libre
    (3, '2A', 80.00, 3, 2, 1, DATE '2025-01-01'),  -- bail 2, TERMINE
    (4, '2B', 30.00, 1, 2, 1, DATE '2025-01-01'),  -- jamais loué
    (5, '1A', 55.00, 2, 1, 2, DATE '2025-06-01'),  -- bail 3, renouvelé
    (6, '2A', 95.00, 4, 2, 2, DATE '2025-06-01'),  -- libre
    (7, '1A', 70.00, 3, 1, 3, DATE '2024-01-01'),  -- bail 4, 3 révisions
    (8, '3A', 110.00, 5, 3, 3, DATE '2024-01-01')  -- bail 5, TERMINE
ON CONFLICT (id_appartement) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  LOCATAIRES (F4)
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO locataire (id_locataire, nom, prenom, email, telephone)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'Traore', 'Kofi',   'kofi.traore@example.test',   '0102030407'),
    (2, 'Diallo', 'Mariam', 'mariam.diallo@example.test', '0102030408'),
    (3, 'Kouassi','Adjoa',  'adjoa.kouassi@example.test', '0102030409'),
    (4, 'Yapo',   'Serge',  'serge.yapo@example.test',    '0102030410'),
    (5, 'Bamba',  'Aicha',  'aicha.bamba@example.test',   '0102030411')
ON CONFLICT (id_locataire) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  BAUX (F5, F8, F10 à F14, F16, F19)
--  Les périodes ne se chevauchent pas : la contrainte EXCLUDE daterange le
--  garantit. Les deux baux échus sont déjà TERMINE pour matérialiser F16.
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO bail (id_bail, date_debut, date_fin, provision_charges, depot_garantie,
                  id_appartement, loyer_mensuel, statut, date_resiliation, date_cloture)
OVERRIDING SYSTEM VALUE VALUES
    (1, DATE '2026-01-01', NULL,          50.00,  900.00, 1, 900.00,  'EN_COURS', NULL, NULL),
    (2, DATE '2025-01-01', DATE '2025-12-31', 40.00, 700.00, 3, 750.00, 'TERMINE', NULL, DATE '2025-12-31'),
    (3, DATE '2026-06-01', DATE '2026-11-30', 60.00, 1000.00, 5, 950.00, 'EN_COURS', NULL, NULL),
    (4, DATE '2024-01-01', NULL,          80.00, 1200.00, 7, 1300.00, 'EN_COURS', NULL, NULL),
    (5, DATE '2025-03-01', DATE '2026-03-31', 100.00, 1500.00, 8, 1400.00, 'TERMINE', NULL, DATE '2026-03-31')
ON CONFLICT (id_bail) DO NOTHING;

INSERT INTO bail_locataire (id_bail, id_locataire) VALUES
    (1, 1), (1, 5),  -- colocation sur le bail 1
    (2, 2), (3, 3), (4, 4), (5, 5)
ON CONFLICT (id_bail, id_locataire) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  RÉVISIONS DE LOYER (F15, F18)
--  V6 n'ayant inséré que les baux présents au moment de la migration, la
--  révision initiale de chaque bail de démo est posée ici explicitement.
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO revision_loyer (id_bail, date_effet, montant) VALUES
    (1, DATE '2026-01-01',  850.00), (1, DATE '2026-07-01',  900.00),
    (2, DATE '2025-01-01',  750.00),
    (3, DATE '2026-06-01',  950.00),
    (4, DATE '2024-01-01', 1100.00), (4, DATE '2025-01-01', 1200.00),
    (4, DATE '2026-01-01', 1300.00),
    (5, DATE '2025-03-01', 1400.00)
ON CONFLICT (id_bail, date_effet) DO NOTHING;

INSERT INTO historique_bail (id_bail, motif, date_evenement, date_fin_avant, date_fin_apres) VALUES
    (2, 'CLOTURE',       DATE '2025-12-31', DATE '2025-12-31', DATE '2025-12-31'),
    (3, 'RENOUVELLEMENT',DATE '2026-10-15', DATE '2026-10-31', DATE '2026-11-30'),
    (5, 'CLOTURE',       DATE '2026-03-31', DATE '2026-03-31', DATE '2026-03-31');

-- ════════════════════════════════════════════════════════════════════════════
--  QUITTANCES ET PAIEMENTS — vue v_solde_quittance
--  La quittance 2 reste impayée pour montrer un reste à payer positif.
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO quittance (id_quittance, mois, loyer_applique, provision_appliquee,
                       date_emission, date_echeance, id_bail, regularisation_appliquee, nature)
OVERRIDING SYSTEM VALUE VALUES
    (1, DATE '2026-10-01',  850.00, 50.00, DATE '2026-10-01', DATE '2026-10-10', 1, 25.00, 'LOYER'),
    (2, DATE '2026-11-01',  900.00, 50.00, DATE '2026-11-01', DATE '2026-11-10', 1,  0.00, 'LOYER'),
    (3, DATE '2026-11-01',  950.00, 60.00, DATE '2026-11-01', DATE '2026-11-10', 3,  0.00, 'LOYER'),
    (4, DATE '2026-11-01', 1300.00, 80.00, DATE '2026-11-01', DATE '2026-11-10', 4,  0.00, 'LOYER')
ON CONFLICT (id_quittance) DO NOTHING;

INSERT INTO paiement (id_paiement, date_paiement, montant, mode, id_quittance)
OVERRIDING SYSTEM VALUE VALUES
    (1, DATE '2026-10-08',  925.00, 'VIREMENT',    1),
    (2, DATE '2026-11-05', 1380.00, 'PRELEVEMENT', 4)
ON CONFLICT (id_paiement) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  CHARGES, RÉGULARISATION, ÉTAT DES LIEUX — vues v_solde_quittance et
--  v_restitution_depot
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO charge_immeuble (id_charge, annee, nature, montant, id_immeuble)
OVERRIDING SYSTEM VALUE VALUES
    (1, 2026, 'Chauffage', 2400.00, 1),
    (2, 2026, 'Entretien',  900.00, 1)
ON CONFLICT (id_charge) DO NOTHING;

INSERT INTO regularisation (id_regularisation, annee, charges_reelles,
                            provisions_versees, solde, id_bail)
OVERRIDING SYSTEM VALUE VALUES
    (1, 2026, 600.00, 600.00, 0.00, 1)
ON CONFLICT (id_regularisation) DO NOTHING;

INSERT INTO piece (id_piece, libelle) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Séjour'), (2, 'Chambre'), (3, 'Cuisine'), (4, 'Salle de bain')
ON CONFLICT (id_piece) DO NOTHING;

INSERT INTO etat_lieux (id_etat, type, date_etat, id_bail)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'ENTREE', DATE '2025-03-01', 5),
    (2, 'SORTIE', DATE '2026-03-31', 5)
ON CONFLICT (id_etat) DO NOTHING;

INSERT INTO constat (id_etat, id_piece, etat, commentaire, cout_degat) VALUES
    (2, 1, 'USE',     'Moquette marquée', 150.00),
    (2, 2, 'BON',     NULL,                 0.00),
    (2, 3, 'DEGRADE', 'Plan de travail rayé', 250.00)
ON CONFLICT (id_etat, id_piece) DO NOTHING;

-- Les identifiants explicites ne doivent pas bloquer les créations via l'API.
SELECT setval(pg_get_serial_sequence('proprietaire', 'id_proprietaire'), 2, true);
SELECT setval(pg_get_serial_sequence('immeuble', 'id_immeuble'), 3, true);
SELECT setval(pg_get_serial_sequence('appartement', 'id_appartement'), 8, true);
SELECT setval(pg_get_serial_sequence('locataire', 'id_locataire'), 5, true);
SELECT setval(pg_get_serial_sequence('bail', 'id_bail'), 5, true);
SELECT setval(pg_get_serial_sequence('historique_bail', 'id_historique'), 3, true);
SELECT setval(pg_get_serial_sequence('revision_loyer', 'id_revision'), 8, true);
SELECT setval(pg_get_serial_sequence('quittance', 'id_quittance'), 4, true);
SELECT setval(pg_get_serial_sequence('paiement', 'id_paiement'), 2, true);
SELECT setval(pg_get_serial_sequence('charge_immeuble', 'id_charge'), 2, true);
SELECT setval(pg_get_serial_sequence('regularisation', 'id_regularisation'), 1, true);
SELECT setval(pg_get_serial_sequence('piece', 'id_piece'), 4, true);
SELECT setval(pg_get_serial_sequence('etat_lieux', 'id_etat'), 2, true);
