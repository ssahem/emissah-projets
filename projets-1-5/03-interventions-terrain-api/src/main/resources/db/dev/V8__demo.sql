-- Données de démonstration — profil "dev" uniquement (jamais en production).
-- Numérotées V8 : la chaîne de migrations versionnées s'arrête à V7.
-- Le référentiel type_intervention est vide dans la base source (E-23) : ce fichier
-- fournit au moins RACCORDEMENT_FIBRE avec ses exigences de compétences (F17).

INSERT INTO region (nom)
SELECT 'Île-de-France' WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Île-de-France');

INSERT INTO departement (nom, id_region)
SELECT 'Paris', r.id_region
FROM region r
WHERE r.nom = 'Île-de-France'
  AND NOT EXISTS (SELECT 1 FROM departement WHERE nom = 'Paris');

INSERT INTO agence (nom, adresse, ville, responsable)
SELECT 'Agence Paris Centre', '10 rue de la Paix', 'Paris', 'Camille Martin'
WHERE NOT EXISTS (SELECT 1 FROM agence WHERE nom = 'Agence Paris Centre');

INSERT INTO commune (nom, code_postal, id_departement, id_agence)
SELECT 'Paris', '75002', d.id_departement, a.id_agence
FROM departement d
CROSS JOIN agence a
WHERE d.nom = 'Paris'
  AND a.nom = 'Agence Paris Centre'
  AND NOT EXISTS (SELECT 1 FROM commune WHERE code_postal = '75002');

INSERT INTO equipe (nom, id_agence)
SELECT 'Equipe Fibre Nord', a.id_agence
FROM agence a
WHERE a.nom = 'Agence Paris Centre'
  AND NOT EXISTS (SELECT 1 FROM equipe WHERE nom = 'Equipe Fibre Nord');

INSERT INTO technicien (nom, prenom, email, telephone, id_equipe, matricule)
SELECT 'Martin', 'Camille', 'camille.martin@example.test', '0102030405', e.id_equipe, 'MAT001'
FROM equipe e
WHERE e.nom = 'Equipe Fibre Nord'
  AND NOT EXISTS (SELECT 1 FROM technicien WHERE matricule = 'MAT001');

INSERT INTO technicien (nom, prenom, email, telephone, id_equipe, matricule)
SELECT 'Bernard', 'Sacha', 'sacha.bernard@example.test', '0102030406', e.id_equipe, 'MAT002'
FROM equipe e
WHERE e.nom = 'Equipe Fibre Nord'
  AND NOT EXISTS (SELECT 1 FROM technicien WHERE matricule = 'MAT002');

INSERT INTO competence (libelle, description)
SELECT 'Raccordement fibre', 'Pose et raccordement de la ligne fibre optique'
WHERE NOT EXISTS (SELECT 1 FROM competence WHERE libelle = 'Raccordement fibre');

INSERT INTO competence (libelle, description)
SELECT 'Securite electrique', 'Habilitation electrique pour interventions sur le reseau'
WHERE NOT EXISTS (SELECT 1 FROM competence WHERE libelle = 'Securite electrique');

INSERT INTO obtention_competence (date_obtention, date_expiration, id_technicien, id_competence)
SELECT DATE '2025-01-10', DATE '2027-01-10', t.id_technicien, c.id_competence
FROM technicien t
CROSS JOIN competence c
WHERE t.matricule = 'MAT001'
  AND c.libelle = 'Raccordement fibre'
  AND NOT EXISTS (
      SELECT 1 FROM obtention_competence o
      WHERE o.id_technicien = t.id_technicien AND o.id_competence = c.id_competence
  );

INSERT INTO obtention_competence (date_obtention, date_expiration, id_technicien, id_competence)
SELECT DATE '2025-03-01', DATE '2026-12-15', t.id_technicien, c.id_competence
FROM technicien t
CROSS JOIN competence c
WHERE t.matricule = 'MAT001'
  AND c.libelle = 'Securite electrique'
  AND NOT EXISTS (
      SELECT 1 FROM obtention_competence o
      WHERE o.id_technicien = t.id_technicien AND o.id_competence = c.id_competence
  );

INSERT INTO type_intervention (libelle, delai_heures, code)
SELECT 'Raccordement fibre', 48, 'RACCORDEMENT_FIBRE'
WHERE NOT EXISTS (SELECT 1 FROM type_intervention WHERE code = 'RACCORDEMENT_FIBRE');

INSERT INTO exigence_competence (id_type, id_competence)
SELECT ti.id_type, c.id_competence
FROM type_intervention ti
CROSS JOIN competence c
WHERE ti.code = 'RACCORDEMENT_FIBRE'
  AND c.libelle IN ('Raccordement fibre', 'Securite electrique')
  AND NOT EXISTS (
      SELECT 1 FROM exigence_competence e
      WHERE e.id_type = ti.id_type AND e.id_competence = c.id_competence
  );

INSERT INTO client (nom, prenom, email, telephone, adresse, commune)
SELECT 'Dupont', 'Marie', 'marie.dupont@example.test', '0102030407', '5 avenue du parc', 'Paris'
WHERE NOT EXISTS (SELECT 1 FROM client WHERE email = 'marie.dupont@example.test');

INSERT INTO piece (code_piece, libelle)
SELECT 'GONDE', 'Gonde de fixation fibre'
WHERE NOT EXISTS (SELECT 1 FROM piece WHERE code_piece = 'GONDE');

INSERT INTO depot (nom, adresse, id_agence)
SELECT 'Depot Paris Centre', '12 rue de la Paix', a.id_agence
FROM agence a
WHERE a.nom = 'Agence Paris Centre'
  AND NOT EXISTS (SELECT 1 FROM depot WHERE nom = 'Depot Paris Centre');

INSERT INTO stock (quantite, id_depot, id_piece)
SELECT 20, d.id_depot, p.id_piece
FROM depot d
CROSS JOIN piece p
WHERE d.nom = 'Depot Paris Centre'
  AND p.code_piece = 'GONDE'
  AND NOT EXISTS (
      SELECT 1 FROM stock s WHERE s.id_depot = d.id_depot AND s.id_piece = p.id_piece
  );
