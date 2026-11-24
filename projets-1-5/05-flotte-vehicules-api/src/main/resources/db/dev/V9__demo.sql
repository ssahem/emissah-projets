-- ==============================================================================
-- V9__demo.sql — jeu de démonstration du profil dev (jamais en production)
--
-- 3 sites, 5 véhicules, 4 salariés, 5 affectations (dont une en cours et une
-- clôturée avec relevé kilométrique), 3 types d'entretien, 4 prestataires,
-- 6 entretiens, des trajets, contrats et dépenses. Les identifiants sont
-- explicites pour rendre la collection Postman reproductible ; les séquences
-- sont repositionnées en fin de fichier.
-- ==============================================================================

INSERT INTO site (id_site, nom, ville, adresse) VALUES
    (1, 'Paris Centre', 'Paris', '12 rue de la Paix'),
    (2, 'Lyon Part-Dieu', 'Lyon', '8 rue de la République'),
    (3, 'Marseille Vieux-Port', 'Marseille', '3 quai des Belges');

INSERT INTO salarie (id_salarie, nom, prenom, email, matricule, service) VALUES
    (1, 'Martin', 'Alice', 'alice.martin@example.test', 'MAT001', 'Direction'),
    (2, 'Bernard', 'Luc', 'luc.bernard@example.test', 'MAT002', 'Technique'),
    (3, 'Dubois', 'Marie', 'marie.dubois@example.test', 'MAT003', 'Comptabilité'),
    (4, 'Petit', 'Paul', 'paul.petit@example.test', 'MAT004', 'Logistique');

INSERT INTO prestataire (id_prestataire, nom, adresse, telephone) VALUES
    (1, 'Garage Central', '10 avenue des Nations', '0102030401'),
    (2, 'Auto Soins', '5 boulevard du Parc', '0102030402'),
    (3, 'Rapide Auto', '2 route de Lyon', '0102030403'),
    (4, 'Concession Nord', '18 rue du Nord', '0102030404');

INSERT INTO type_entretien (id_type_entretien, libelle, periodicite_km, periodicite_mois, categorie) VALUES
    (1, 'Vidange périodique', 15000, 12, 'PERIODIQUE'),
    (2, 'Révision périodique', 15000, 12, 'PERIODIQUE'),
    (3, 'Remplacement pneus', 30000, 24, 'CORRECTIF');

INSERT INTO type_depense (id_type_depense, libelle, avec_conducteur) VALUES
    (1, 'Carburant et péage', true),
    (2, 'Assurance', false);

INSERT INTO vehicule (id_vehicule, immatriculation, marque, modele, date_mise_en_service,
                      mode_acquisition, partage, prix_achat, id_site, id_vehicule_remplace) VALUES
    (1, 'AB-123-CD', 'Peugeot', '308', '2024-01-15', 'PROPRIETE', false, 25000.00, 1, NULL),
    (2, 'CD-456-EF', 'Renault', 'Clio', '2024-03-01', 'LOCATION_LONGUE_DUREE', true, NULL, 1, NULL),
    (3, 'EF-789-GH', 'Toyota', 'Yaris', '2023-06-10', 'PROPRIETE', false, 18000.00, 2, NULL),
    (4, 'GH-012-IJ', 'Ford', 'Focus', '2025-02-20', 'LEASING', false, NULL, 2, NULL),
    (5, 'IJ-345-KL', 'Tesla', 'Model 3', '2025-09-01', 'PROPRIETE', false, 42000.00, 3, 3);

INSERT INTO affectation (id_affectation, date_debut, date_fin, id_vehicule, id_salarie, km_restitution) VALUES
    (1, '2026-09-01', '2026-09-30', 1, 1, 18500),
    (2, '2026-11-01', NULL, 2, 2, NULL),
    (3, '2026-11-01', '2026-11-15', 3, 3, 42500),
    (4, '2026-12-01', '2026-12-15', 4, 4, NULL),
    (5, '2026-10-01', '2026-10-31', 5, 1, 12000);

INSERT INTO entretien (id_entretien, date_debut, date_fin, cout, km_compteur,
                       id_vehicule, id_type_entretien, id_prestataire) VALUES
    (1, '2025-06-10', '2025-06-11', 320.00, 12000, 1, 1, 1),
    (2, '2026-10-15', '2026-10-16', 280.00, 19000, 1, 2, 2),
    (3, '2026-08-05', NULL, 150.00, 8000, 2, 3, 3),
    (4, '2026-09-12', NULL, 450.00, 40000, 3, 1, 1),
    (5, '2026-07-20', NULL, 210.00, 15000, 4, 2, 4),
    (6, '2026-11-10', '2026-11-12', 600.00, 13000, 5, 1, 2);

INSERT INTO trajet (id_trajet, date_trajet, km_depart, km_arrivee, motif, id_affectation) VALUES
    (1, '2026-09-10', 17000, 18100, 'Livraison', 1),
    (2, '2026-11-10', 8200, 8500, 'Tournée', 2),
    (3, '2026-11-05', 40500, 41200, 'Visite client', 3),
    (4, '2026-10-20', 11500, 11900, 'Déplacement', 5);

INSERT INTO contrat (id_contrat, date_debut, duree_mois, loyer_mensuel, km_autorise, id_vehicule) VALUES
    (1, '2024-03-01', 36, 350.00, 20000, 2),
    (2, '2025-02-20', 48, 420.00, 30000, 4);

INSERT INTO depense (id_depense, date_depense, montant, commentaire, id_vehicule, id_type_depense) VALUES
    (1, '2026-10-02', 89.90, 'Lavage', 1, 1),
    (2, '2026-11-03', 120.00, 'Péage', 2, 1),
    (3, '2026-09-18', 45.00, 'Contrôle technique', 3, 2);

-- Alerte déjà émise pour le véhicule 2 (échéance dépassée) : F16 ne la duplicate pas.
INSERT INTO alerte_vehicule (id_alerte, id_affectation, id_vehicule, motif, periode,
                             date_alerte, date_echeance, ecart_km, ecart_jours, detail) VALUES
    (1, 2, 2, 'ECHEANCE_ENTRETIEN', '2026-11', '2026-11-24', '2025-03-01', 8500, 633,
     'Véhicule CD-456-EF : échéance d''entretien dépassée');

-- Les insertions avec identifiant explicite n'avancent pas les séquences d'identité.
SELECT setval(pg_get_serial_sequence('site', 'id_site'), (SELECT MAX(id_site) FROM site));
SELECT setval(pg_get_serial_sequence('salarie', 'id_salarie'), (SELECT MAX(id_salarie) FROM salarie));
SELECT setval(pg_get_serial_sequence('prestataire', 'id_prestataire'), (SELECT MAX(id_prestataire) FROM prestataire));
SELECT setval(pg_get_serial_sequence('type_entretien', 'id_type_entretien'), (SELECT MAX(id_type_entretien) FROM type_entretien));
SELECT setval(pg_get_serial_sequence('type_depense', 'id_type_depense'), (SELECT MAX(id_type_depense) FROM type_depense));
SELECT setval(pg_get_serial_sequence('vehicule', 'id_vehicule'), (SELECT MAX(id_vehicule) FROM vehicule));
SELECT setval(pg_get_serial_sequence('affectation', 'id_affectation'), (SELECT MAX(id_affectation) FROM affectation));
SELECT setval(pg_get_serial_sequence('entretien', 'id_entretien'), (SELECT MAX(id_entretien) FROM entretien));
SELECT setval(pg_get_serial_sequence('contrat', 'id_contrat'), (SELECT MAX(id_contrat) FROM contrat));
SELECT setval(pg_get_serial_sequence('depense', 'id_depense'), (SELECT MAX(id_depense) FROM depense));
SELECT setval(pg_get_serial_sequence('trajet', 'id_trajet'), (SELECT MAX(id_trajet) FROM trajet));
SELECT setval(pg_get_serial_sequence('alerte_vehicule', 'id_alerte'), (SELECT MAX(id_alerte) FROM alerte_vehicule));
