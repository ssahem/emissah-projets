-- E-01/E-02/E-03 : ville et responsable d'agence, nom unique.
ALTER TABLE agence ADD COLUMN ville VARCHAR(100);
ALTER TABLE agence ADD COLUMN responsable VARCHAR(100);

UPDATE agence SET ville = 'Non renseigné' WHERE ville IS NULL;

ALTER TABLE agence ALTER COLUMN ville SET NOT NULL;
ALTER TABLE agence ADD CONSTRAINT uq_agence_nom UNIQUE (nom);
