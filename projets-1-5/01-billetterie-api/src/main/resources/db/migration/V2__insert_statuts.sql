-- ==============================================================================
--  V2 — Statuts initiaux
--  But : disposer exactement des statuts métier utilisés par les règles de
--  réservation. ON CONFLICT rend la migration idempotente sur une base existante.
-- ==============================================================================

INSERT INTO statut (libelle) VALUES ('PROVISOIRE'), ('PAYEE'), ('ANNULEE')
ON CONFLICT (libelle) DO NOTHING;
