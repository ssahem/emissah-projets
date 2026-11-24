-- Données de démonstration — profil "dev" uniquement (jamais en production).
-- Numérotées V7 : la chaîne de migrations versionnées s'arrête à V6.
-- Jeu volontairement complet : salles, catégories, tarifs, spectacles,
-- événements futurs et passé, places, clients et réservations dans les trois
-- statuts (PROVISOIRE, PAYEE, ANNULEE). Il couvre les requêtes F1 à F19.

-- ════════════════════════════════════════════════════════════════════════════
--  CATÉGORIES ET TARIFS
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO categorie (id_categorie, libelle) OVERRIDING SYSTEM VALUE VALUES
    (1, 'ORCHESTRE'),
    (2, 'BALCON'),
    (3, 'LOGE')
ON CONFLICT (id_categorie) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  SALLES
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO salle (id_salle, nom, adresse, ville, capacite) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Grand Hall', '12 rue des Arts', 'Abidjan', 200),
    (2, 'Théâtre National', '1 avenue de la Culture', 'Abidjan', 120)
ON CONFLICT (id_salle) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  SPECTACLES ET ÉVÉNEMENTS
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO spectacle (id_spectacle, titre, type, placement_numerote) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Concert Jazz', 'CONCERT', true),
    (2, 'Hamlet', 'THEATRE', true),
    (3, 'Conférence IA', 'CONFERENCE', true)
ON CONFLICT (id_spectacle) DO NOTHING;

INSERT INTO evenement (id_evenement, date_heure, id_spectacle, id_salle) OVERRIDING SYSTEM VALUE VALUES
    (1, TIMESTAMP '2026-12-20 20:00:00', 1, 1),
    (2, TIMESTAMP '2026-10-05 20:00:00', 2, 2),
    (3, TIMESTAMP '2026-12-27 19:00:00', 3, 1)
ON CONFLICT (id_evenement) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  PLACES — 12 places au Grand Hall, 6 au Théâtre National
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO place (id_place, rang, numero, id_salle, id_categorie) OVERRIDING SYSTEM VALUE VALUES
    (1,  'A', 1, 1, 1), (2,  'A', 2, 1, 1), (3,  'A', 3, 1, 1),
    (4,  'A', 4, 1, 2), (5,  'A', 5, 1, 2), (6,  'A', 6, 1, 2),
    (7,  'B', 1, 1, 2), (8,  'B', 2, 1, 2), (9,  'B', 3, 1, 2),
    (10, 'B', 4, 1, 3), (11, 'B', 5, 1, 3), (12, 'B', 6, 1, 3),
    (13, 'A', 1, 2, 1), (14, 'A', 2, 2, 1), (15, 'A', 3, 2, 1),
    (16, 'A', 4, 2, 2), (17, 'A', 5, 2, 2), (18, 'A', 6, 2, 2)
ON CONFLICT (id_place) DO NOTHING;

INSERT INTO tarif (id_tarif, id_evenement, categorie, prix) OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 'ORCHESTRE', 15000.00),
    (2, 1, 'BALCON',    10000.00),
    (3, 1, 'LOGE',       8000.00),
    (4, 2, 'ORCHESTRE', 12000.00),
    (5, 2, 'BALCON',     8000.00),
    (6, 3, 'ORCHESTRE',  5000.00),
    (7, 3, 'BALCON',     3000.00)
ON CONFLICT (id_tarif) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  CLIENTS
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO client (id_client, nom, prenom, email) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Dupont',  'Jean',  'jean.dupont@example.test'),
    (2, 'Kouassi', 'Marie', 'marie.kouassi@example.test'),
    (3, 'Diallo',  'Awa',   'awa.diallo@example.test')
ON CONFLICT (id_client) DO NOTHING;

-- ════════════════════════════════════════════════════════════════════════════
--  RÉSERVATIONS ET STATUTS
--  La réservation 1 est provisoire et expirée (F16), la 2 est payée (F14),
--  la 3 est annulée avec remboursement (F15), la 4 est payée sur un autre
--  événement. Les positions sont distinctes par événement (F11).
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO reservation (id_reservation, date_creation,
                         id_client, id_evenement, montant_rembourse, date_paiement)
OVERRIDING SYSTEM VALUE
SELECT 1, TIMESTAMP '2026-11-20 10:00:00', 1, 1, 0.00, NULL
WHERE NOT EXISTS (SELECT 1 FROM reservation WHERE id_reservation = 1);

INSERT INTO reservation (id_reservation, date_creation,
                         id_client, id_evenement, montant_rembourse, date_paiement)
OVERRIDING SYSTEM VALUE
SELECT 2, TIMESTAMP '2026-11-21 11:00:00', 2, 1, 0.00,
       TIMESTAMP '2026-11-21 11:05:00'
WHERE NOT EXISTS (SELECT 1 FROM reservation WHERE id_reservation = 2);

INSERT INTO reservation (id_reservation, date_creation,
                         id_client, id_evenement, montant_rembourse, date_paiement)
OVERRIDING SYSTEM VALUE
SELECT 3, TIMESTAMP '2026-10-01 09:00:00', 3, 2, 12000.00, NULL
WHERE NOT EXISTS (SELECT 1 FROM reservation WHERE id_reservation = 3);

INSERT INTO reservation (id_reservation, date_creation,
                         id_client, id_evenement, montant_rembourse, date_paiement)
OVERRIDING SYSTEM VALUE
SELECT 4, TIMESTAMP '2026-11-22 14:00:00', 1, 3, 0.00,
       TIMESTAMP '2026-11-22 14:02:00'
WHERE NOT EXISTS (SELECT 1 FROM reservation WHERE id_reservation = 4);

INSERT INTO reservation_place (id_reservation_place, id_reservation, rang, numero,
                               prix_applique, id_evenement) OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 'A', 1, 15000.00, 1),
    (2, 1, 'A', 2, 15000.00, 1),
    (3, 2, 'A', 3, 15000.00, 1),
    (4, 3, 'A', 1, 12000.00, 2),
    (5, 4, 'B', 1,  3000.00, 3)
ON CONFLICT (id_reservation_place) DO NOTHING;

INSERT INTO historique_statut (id_reservation, id_statut, date_statut)
SELECT r.id_reservation, s.id_statut, r.date_creation
FROM reservation r
JOIN statut s ON s.libelle = CASE r.id_reservation
                                WHEN 1 THEN 'PROVISOIRE'
                                WHEN 2 THEN 'PAYEE'
                                WHEN 3 THEN 'ANNULEE'
                                WHEN 4 THEN 'PAYEE'
                             END
WHERE r.id_reservation BETWEEN 1 AND 4
ON CONFLICT (id_reservation, id_statut) DO NOTHING;


-- date_expiration est générée à partir de date_creation dans le schéma source ;
-- elle est simple avec une valeur par défaut dans V1. On l'aligne dans les deux cas.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_name = 'reservation'
                  AND column_name = 'date_expiration'
                  AND is_generated = 'NEVER') THEN
        UPDATE reservation
           SET date_expiration = date_creation + interval '15 minutes'
         WHERE id_reservation BETWEEN 1 AND 4;
    END IF;
END $$;

-- Les identifiants explicites ci-dessus doivent laisser les séquences prêtes
-- pour les créations passant par l'API.
SELECT setval(pg_get_serial_sequence('categorie', 'id_categorie'), 3, true);
SELECT setval(pg_get_serial_sequence('salle', 'id_salle'), 2, true);
SELECT setval(pg_get_serial_sequence('spectacle', 'id_spectacle'), 3, true);
SELECT setval(pg_get_serial_sequence('evenement', 'id_evenement'), 3, true);
SELECT setval(pg_get_serial_sequence('place', 'id_place'), 18, true);
SELECT setval(pg_get_serial_sequence('tarif', 'id_tarif'), 7, true);
SELECT setval(pg_get_serial_sequence('client', 'id_client'), 3, true);
SELECT setval(pg_get_serial_sequence('reservation', 'id_reservation'), 4, true);
SELECT setval(pg_get_serial_sequence('reservation_place', 'id_reservation_place'), 5, true);
