-- ==============================================================================
-- V4__obligatoires.sql — champs obligatoires de l'énoncé (F1 et F3)
--
-- V2 ajoute site.ville, salarie.matricule et salarie.service sans NOT NULL :
-- information_schema les déclare NULLABLE, alors que l'énoncé les rend
-- obligatoires. Les données de démonstration ne contiennent aucune valeur nulle
-- pour ces trois colonnes. V2 restant inchangée (checksum Flyway), la contrainte
-- est posée ici.
-- ==============================================================================

ALTER TABLE site ALTER COLUMN ville SET NOT NULL;
ALTER TABLE salarie ALTER COLUMN matricule SET NOT NULL;
ALTER TABLE salarie ALTER COLUMN service SET NOT NULL;
