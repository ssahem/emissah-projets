-- E-07 : les seules colonnes exigées par l'énoncé et réellement nullables dans
-- le schéma source (vérification information_schema.columns.is_nullable du
-- 24 novembre 2026, consignée dans le registre d'audit).

ALTER TABLE proprietaire ALTER COLUMN telephone SET NOT NULL;
ALTER TABLE locataire ALTER COLUMN telephone SET NOT NULL;
