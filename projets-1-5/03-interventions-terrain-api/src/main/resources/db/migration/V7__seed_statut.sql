-- E-14/E-22 : référentiel des statuts. Insertion idempotente : sur une base vierge les cinq
-- libellés sont créés ; sur la base source seul ANNULEE manquait et est ajouté.
-- EN_ROUTE et REPORTEE, valeurs héritées, sont conservées sans modification.
INSERT INTO statut (libelle)
VALUES ('PLANIFIEE'), ('EN_COURS'), ('TERMINEE'), ('ECHOUEE'), ('ANNULEE')
ON CONFLICT (libelle) DO NOTHING;
