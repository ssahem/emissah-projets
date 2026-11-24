-- E-02 : nombre d'étages d'un immeuble, exigé à la création (F2) et comparé aux
-- étages des appartements (F2, F3). Le DEFAULT sert au backfill puis est retiré.

ALTER TABLE immeuble ADD COLUMN nb_etages INTEGER NOT NULL DEFAULT 1;

UPDATE immeuble i
SET nb_etages = GREATEST(1, COALESCE((
    SELECT MAX(a.etage)
    FROM appartement a
    WHERE a.id_immeuble = i.id_immeuble), 1));

ALTER TABLE immeuble ALTER COLUMN nb_etages DROP DEFAULT;

ALTER TABLE immeuble ADD CONSTRAINT ck_immeuble_nb_etages CHECK (nb_etages > 0);
