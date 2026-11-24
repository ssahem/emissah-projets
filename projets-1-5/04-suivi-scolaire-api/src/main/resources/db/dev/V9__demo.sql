-- ==============================================================================
-- V9__demo.sql — données de démonstration (profil "dev" uniquement)
--
-- Jeu complet pour F1 à F19 : 2 années, 2 niveaux, 3 classes, 6 élèves,
-- 4 matières, 3 professeurs, 9 enseignements, 3 trimestres dont le premier est
-- clôturé, 3 types d'évaluation, 15 notes, 1 alerte et 1 réouverture journalisée.
-- Les identifiants sont fixés explicitement pour référencer les données de façon
-- stable dans la collection Postman et les tests d'intégration.
-- ==============================================================================

INSERT INTO annee_scolaire (id_annee, libelle, date_debut, date_fin, seuil_alerte_absences) VALUES
    (1, '2025-2026', DATE '2025-09-01', DATE '2026-06-30', 3),
    (2, '2026-2027', DATE '2026-09-01', DATE '2027-06-30', 3);

INSERT INTO niveau (id_niveau, libelle, code) VALUES
    (1, 'Sixième', '6E'),
    (2, 'Cinquième', '5E');

INSERT INTO classe (id_classe, nom, id_annee, id_niveau, effectif_max) VALUES
    (1, '6A', 2, 1, 3),
    (2, '6B', 2, 1, 3),
    (3, '5A', 2, 2, 3);

INSERT INTO eleve (id_eleve, nom, prenom, date_naissance) VALUES
    (1, 'Kouassi', 'Aya', DATE '2011-02-14'),
    (2, 'Dupont', 'Lucas', DATE '2011-05-03'),
    (3, 'Traore', 'Aminata', DATE '2011-08-21'),
    (4, 'Bernard', 'Nina', DATE '2010-11-09'),
    (5, 'Yao', 'Kofi', DATE '2010-04-17'),
    (6, 'Martin', 'Emma', DATE '2010-07-12');

INSERT INTO inscription (id_inscription, date_inscription, id_eleve, id_classe,
                         numero_inscription, date_fin) VALUES
    (1, DATE '2026-09-02', 1, 1, 'INS-2026-001', NULL),
    (2, DATE '2026-09-02', 2, 1, 'INS-2026-002', NULL),
    (3, DATE '2026-09-02', 3, 1, 'INS-2026-003', NULL),
    (4, DATE '2026-09-02', 4, 2, 'INS-2026-004', NULL),
    (5, DATE '2026-09-02', 5, 3, 'INS-2026-005', NULL),
    (6, DATE '2026-09-02', 6, 3, 'INS-2026-006', NULL),
    (7, DATE '2025-09-02', 5, 1, 'INS-2025-005', DATE '2026-06-30');

INSERT INTO matiere (id_matiere, libelle, code, coefficient) VALUES
    (1, 'Mathematiques', 'MATH', 4),
    (2, 'Francais', 'FRAN', 3),
    (3, 'Histoire-Geographie', 'HIST', 2),
    (4, 'Anglais', 'ANGL', 2);

INSERT INTO professeur (id_professeur, nom, prenom, email, telephone) VALUES
    (1, 'Durand', 'Claire', 'claire.durand@example.test', '0102030401'),
    (2, 'Koffi', 'Marc', 'marc.koffi@example.test', '0102030402'),
    (3, 'Petit', 'Julie', 'julie.petit@example.test', '0102030403');

INSERT INTO enseignement (id_enseignement, id_classe, id_matiere, id_professeur) VALUES
    (1, 1, 1, 1),
    (2, 1, 2, 2),
    (3, 1, 3, 3),
    (4, 2, 1, 1),
    (5, 2, 2, 2),
    (6, 2, 4, 3),
    (7, 3, 1, 1),
    (8, 3, 2, 2),
    (9, 3, 4, 3);

INSERT INTO trimestre (id_trimestre, numero, date_debut, date_fin, id_annee,
                       est_cloture, date_cloture) VALUES
    (1, 1, DATE '2026-09-01', DATE '2026-11-15', 2, true, DATE '2026-11-16'),
    (2, 2, DATE '2026-11-16', DATE '2027-02-15', 2, false, NULL),
    (3, 3, DATE '2027-02-16', DATE '2027-06-30', 2, false, NULL);

INSERT INTO type_evaluation (id_type_eval, libelle, poids) VALUES
    (1, 'Devoir', 1),
    (2, 'Interrogation', 2),
    (3, 'Oral', 1.5);

INSERT INTO coefficient (id_coefficient, valeur, id_matiere, code_niveau) VALUES
    (1, 4, 1, '6E'), (2, 3, 2, '6E'), (3, 2, 3, '6E'), (4, 2, 4, '6E'),
    (5, 4, 1, '5E'), (6, 3, 2, '5E'), (7, 2, 3, '5E'), (8, 2, 4, '5E');

-- Trimestre 1 : notes figées (clôturé), 4 notes.
INSERT INTO note (id_note, valeur, numero_trimestre, id_type_eval,
                  id_enseignement, numero_inscription) VALUES
    (1, 16, 1, 1, 1, 'INS-2026-001'),
    (2, 12, 1, 2, 2, 'INS-2026-001'),
    (3, 14, 1, 3, 3, 'INS-2026-001'),
    (4, 10, 1, 1, 4, 'INS-2026-002');

-- Trimestre 2 : 11 notes, dont une chute et une moyenne inférieure à 8.
INSERT INTO note (id_note, valeur, numero_trimestre, id_type_eval,
                  id_enseignement, numero_inscription) VALUES
    (5, 10, 2, 1, 1, 'INS-2026-001'),
    (6, 7, 2, 2, 2, 'INS-2026-001'),
    (7, 8, 2, 3, 3, 'INS-2026-001'),
    (8, 4, 2, 1, 1, 'INS-2026-002'),
    (9, 3, 2, 2, 2, 'INS-2026-002'),
    (10, 5, 2, 3, 3, 'INS-2026-002'),
    (11, 12, 2, 1, 4, 'INS-2026-003'),
    (12, 15, 2, 2, 5, 'INS-2026-003'),
    (13, 9, 2, 1, 7, 'INS-2026-005'),
    (14, 11, 2, 2, 8, 'INS-2026-005'),
    (15, 14, 2, 1, 9, 'INS-2026-006');

-- Une alerte déjà produite (non dupliquée par la tâche planifiée).
INSERT INTO alerte_difficulte (id_alerte, id_inscription, numero_trimestre,
                               moyenne, ecart, date_alerte) VALUES
    (1, 2, 2, 4.08, 0, DATE '2026-11-23');

-- Traçabilité d'une réouverture du premier trimestre.
INSERT INTO historique_reouverture_trimestre
    (id_historique, id_trimestre, date_reouverture, motif, auteur) VALUES
    (1, 1, DATE '2026-11-20',
     'Correction d une erreur de saisie sur une note de mathmatiques', 'Responsable scolarite');

-- Les identifiants étant fixés explicitement, les séquences d'identité restent
-- à leur valeur initiale : sans ce réglage, le premier INSERT via l'API renverrait
-- une erreur 409 sur une clé primaire déjà utilisée.
SELECT setval(pg_get_serial_sequence('annee_scolaire', 'id_annee'), (SELECT MAX(id_annee) FROM annee_scolaire));
SELECT setval(pg_get_serial_sequence('niveau', 'id_niveau'), (SELECT MAX(id_niveau) FROM niveau));
SELECT setval(pg_get_serial_sequence('classe', 'id_classe'), (SELECT MAX(id_classe) FROM classe));
SELECT setval(pg_get_serial_sequence('eleve', 'id_eleve'), (SELECT MAX(id_eleve) FROM eleve));
SELECT setval(pg_get_serial_sequence('inscription', 'id_inscription'), (SELECT MAX(id_inscription) FROM inscription));
SELECT setval(pg_get_serial_sequence('matiere', 'id_matiere'), (SELECT MAX(id_matiere) FROM matiere));
SELECT setval(pg_get_serial_sequence('professeur', 'id_professeur'), (SELECT MAX(id_professeur) FROM professeur));
SELECT setval(pg_get_serial_sequence('enseignement', 'id_enseignement'), (SELECT MAX(id_enseignement) FROM enseignement));
SELECT setval(pg_get_serial_sequence('trimestre', 'id_trimestre'), (SELECT MAX(id_trimestre) FROM trimestre));
SELECT setval(pg_get_serial_sequence('type_evaluation', 'id_type_eval'), (SELECT MAX(id_type_eval) FROM type_evaluation));
SELECT setval(pg_get_serial_sequence('coefficient', 'id_coefficient'), (SELECT MAX(id_coefficient) FROM coefficient));
SELECT setval(pg_get_serial_sequence('note', 'id_note'), (SELECT MAX(id_note) FROM note));
SELECT setval(pg_get_serial_sequence('alerte_difficulte', 'id_alerte'), (SELECT MAX(id_alerte) FROM alerte_difficulte));
SELECT setval(pg_get_serial_sequence('historique_reouverture_trimestre', 'id_historique'), (SELECT MAX(id_historique) FROM historique_reouverture_trimestre));
