-- =============================================================================
--  V8 — Date de création d'un appartement (F18)
-- =============================================================================
--  POURQUOI : F18 doit restituer les vacances "depuis la création" d'un
--             logement jamais loué. V1 est un baseline et ne doit jamais être
--             modifié : l'attribut simple est donc ajouté ici.
--  VALEUR    : CURRENT_DATE pour les lignes existantes ; l'application fournit
--              explicitement LocalDate.now(clock) pour les nouvelles lignes,
--              afin que les tests et la règle métier partagent la même horloge.
-- =============================================================================

ALTER TABLE appartement
    ADD COLUMN date_creation DATE NOT NULL DEFAULT CURRENT_DATE;

COMMENT ON COLUMN appartement.date_creation IS
    'Date de mise en service du logement : borne initiale de la vacance F18';
