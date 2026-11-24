-- E-06 : adresse et commune du client (champs texte, sans FK).
-- E-20 : le téléphone devient obligatoire comme le demande F4.
ALTER TABLE client ADD COLUMN adresse VARCHAR(255);
ALTER TABLE client ADD COLUMN commune VARCHAR(100);

UPDATE client SET adresse = 'Non renseigné' WHERE adresse IS NULL OR btrim(adresse) = '';
UPDATE client SET commune = 'Non renseigné' WHERE commune IS NULL OR btrim(commune) = '';
UPDATE client SET telephone = 'Non renseigné' WHERE telephone IS NULL OR btrim(telephone) = '';

ALTER TABLE client ALTER COLUMN adresse SET NOT NULL;
ALTER TABLE client ALTER COLUMN commune SET NOT NULL;
ALTER TABLE client ALTER COLUMN telephone SET NOT NULL;
