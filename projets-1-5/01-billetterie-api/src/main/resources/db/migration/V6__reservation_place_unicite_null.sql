-- ==============================================================================
--  V6 — Arbitrage E-04 : unicité (événement, position) y compris pour les NULL
--  V4 a posé UNIQUE (id_evenement, rang, numero). PostgreSQL ignore les lignes
--  contenant un NULL dans une contrainte UNIQUE classique : deux lignes avec
--  rang/numero NULL pour le même événement pouvaient donc coexister.
--  Cette migration complète V4 sans la modifier :
--   1. elle rejoue le backfill de id_evenement pour les lignes restantes ;
--   2. elle rend id_evenement NOT NULL (toute ligne a une réservation parente) ;
--   3. elle ajoute un index unique sur des expressions COALESCE, qui couvre
--      aussi les positions NULL.
-- ==============================================================================

UPDATE reservation_place rp
   SET id_evenement = r.id_evenement
  FROM reservation r
 WHERE rp.id_reservation = r.id_reservation
   AND rp.id_evenement IS NULL;

ALTER TABLE reservation_place ALTER COLUMN id_evenement SET NOT NULL;

CREATE UNIQUE INDEX uk_reservation_place_evenement_position
    ON reservation_place (
        id_evenement,
        COALESCE(rang, ''),
        COALESCE(numero, -1)
    );
