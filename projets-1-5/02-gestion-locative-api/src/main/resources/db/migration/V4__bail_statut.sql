-- E-04 : cycle de vie explicite du bail. « Pas commencé » reste déduit de
-- date_debut ; un bail échéant aujourd'hui reste EN_COURS (F16).

ALTER TABLE bail
    ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
    ADD COLUMN date_resiliation DATE,
    ADD COLUMN date_cloture DATE;

ALTER TABLE bail ADD CONSTRAINT ck_bail_statut
    CHECK (statut IN ('EN_COURS', 'RESILIE', 'TERMINE'));

-- Approximation de backfill : la date réelle de clôture n'existait pas avant V4.
UPDATE bail
SET statut = 'TERMINE', date_cloture = date_fin
WHERE date_fin IS NOT NULL AND date_fin < CURRENT_DATE;

ALTER TABLE bail ALTER COLUMN statut DROP DEFAULT;
