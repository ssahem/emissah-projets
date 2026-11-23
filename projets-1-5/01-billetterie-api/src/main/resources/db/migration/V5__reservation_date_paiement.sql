-- ==============================================================================
--  V5 — Arbitrage E-03 : date de paiement
--  But : enregistrer l'instant exact du passage au statut PAYEE (F14).
-- ==============================================================================

ALTER TABLE reservation ADD COLUMN date_paiement timestamp;
