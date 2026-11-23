-- ==============================================================================
--  V3 — Arbitrage E-01 : une salle est identifiée par (nom, ville)
--  La colonne adresse est conservée ; ville est backfillée depuis adresse.
--  Attention : si adresse contient une adresse complète, le backfill donne une
--  ville approximative — à vérifier sur les données réelles avant déploiement.
-- ==============================================================================

ALTER TABLE salle ADD COLUMN ville varchar(100);

UPDATE salle SET ville = adresse WHERE ville IS NULL;

ALTER TABLE salle ALTER COLUMN ville SET NOT NULL;

ALTER TABLE salle ADD CONSTRAINT uk_salle_nom_ville UNIQUE (nom, ville);
