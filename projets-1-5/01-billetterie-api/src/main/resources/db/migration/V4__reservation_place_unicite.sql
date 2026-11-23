-- ==============================================================================
--  V4 — Arbitrage E-02 : unicité (événement, place)
--  id_evenement est dénormalisé dans reservation_place pour porter l'unicité.
--  Les lignes existantes sont backfillées depuis la réservation parente.
--  Attention : rang et numero sont nullables ; une UNIQUE classique ignore les
--  lignes contenant un NULL. Le contrôle applicatif couvre ce cas.
-- ==============================================================================

ALTER TABLE reservation_place ADD COLUMN id_evenement integer;

UPDATE reservation_place rp
   SET id_evenement = r.id_evenement
  FROM reservation r
 WHERE rp.id_reservation = r.id_reservation;

ALTER TABLE reservation_place
    ADD CONSTRAINT fk_reservation_place_evenement FOREIGN KEY (id_evenement) REFERENCES evenement(id_evenement);

ALTER TABLE reservation_place
    ADD CONSTRAINT uk_reservation_place_evenement UNIQUE (id_evenement, rang, numero);
