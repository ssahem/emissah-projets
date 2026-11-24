-- E-01 : le lien immeuble ↔ propriétaire passe par immeuble.
-- Contrôles à exécuter avant le backfill : s'ils renvoient des lignes, on s'arrête
-- et on signale ; aucune valeur n'est choisie arbitrairement.
--
-- SELECT id_immeuble FROM appartement
--  GROUP BY id_immeuble HAVING COUNT(DISTINCT id_proprietaire) > 1;
-- SELECT i.id_immeuble FROM immeuble i
--  WHERE NOT EXISTS (SELECT 1 FROM appartement a WHERE a.id_immeuble = i.id_immeuble);

ALTER TABLE immeuble ADD COLUMN id_proprietaire INTEGER;

UPDATE immeuble i
SET id_proprietaire = (
    SELECT MIN(a.id_proprietaire)
    FROM appartement a
    WHERE a.id_immeuble = i.id_immeuble
);

ALTER TABLE immeuble ALTER COLUMN id_proprietaire SET NOT NULL;

ALTER TABLE immeuble ADD CONSTRAINT fk_immeuble_proprietaire
    FOREIGN KEY (id_proprietaire) REFERENCES proprietaire(id_proprietaire);

-- La suppression de la colonne emporte sa contrainte FK.
ALTER TABLE appartement DROP COLUMN id_proprietaire;
