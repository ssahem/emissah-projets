-- Données de démonstration — profil "dev" uniquement (jamais en production).
-- Numérotées V8 pour être appliquées après V1 à V7, sans collision de version.

INSERT INTO proprietaire (nom, prenom, email, telephone)
VALUES ('Diallo', 'Awa', 'awa.diallo@example.test', '0102030405');

INSERT INTO immeuble (adresse, code_postal, ville, id_proprietaire, nb_etages)
VALUES ('12 rue des Acacias', '75001', 'Paris', 1, 3);

INSERT INTO appartement (numero, surface, nb_pieces, etage, id_immeuble)
VALUES ('1A', 45.50, 2, 1, 1);

INSERT INTO locataire (nom, prenom, email, telephone)
VALUES ('Traore', 'Kofi', 'kofi.traore@example.test', '0102030406');

INSERT INTO bail (date_debut, date_fin, provision_charges, depot_garantie, id_appartement, loyer_mensuel, statut)
VALUES (DATE '2026-01-01', NULL, 50.00, 900.00, 1, 850.00, 'EN_COURS');

INSERT INTO bail_locataire (id_bail, id_locataire)
VALUES (1, 1);

-- La révision initiale de V6 ne couvre que les baux déjà présents : on complète
-- pour le bail de démonstration afin que F15 et F18 restent cohérents.
INSERT INTO revision_loyer (id_bail, date_effet, montant)
SELECT b.id_bail, b.date_debut, b.loyer_mensuel
FROM bail b
WHERE NOT EXISTS (SELECT 1 FROM revision_loyer r WHERE r.id_bail = b.id_bail);
