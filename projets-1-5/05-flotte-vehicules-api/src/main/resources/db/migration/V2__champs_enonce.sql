-- ==============================================================================
-- V2__champs_enonce.sql — attributs demanded par F1, F3, F5, F13 et F14
--
-- Ajoute site.ville, l'unicité de site.nom, salarie.matricule (unique),
-- salarie.service, affectation.km_restitution et resserre entretien_cout_check
-- à cout > 0. Les contraintes déjà présentes dans V1 ne sont pas dupliquées.
-- Les deux vues qui exposent un compteur sont remplacées pour intégrer le
-- relevé de restitution ; leurs colonnes et leurs types restent identiques.
-- ==============================================================================

ALTER TABLE site ADD COLUMN ville character varying;
ALTER TABLE site ADD CONSTRAINT uq_site_nom UNIQUE (nom);

ALTER TABLE salarie ADD COLUMN matricule character varying;
ALTER TABLE salarie ADD CONSTRAINT uq_salarie_matricule UNIQUE (matricule);
ALTER TABLE salarie ADD COLUMN service character varying;

ALTER TABLE affectation ADD COLUMN km_restitution integer;

ALTER TABLE entretien DROP CONSTRAINT entretien_cout_check;
ALTER TABLE entretien ADD CONSTRAINT entretien_cout_check CHECK ((cout > (0)::numeric));

-- ══════════════════════════════════════════════════════════════════════════════
--  VUES MISES À JOUR (mêmes colonnes, même ordre, mêmes types qu'en V1)
-- ══════════════════════════════════════════════════════════════════════════════

-- D-02 : sans date de fin, l'immobilisation dure une seule journée (date_debut).
CREATE OR REPLACE VIEW v_conflit_affectation_entretien AS
SELECT a.id_affectation, e.id_entretien, a.id_vehicule
FROM affectation a
JOIN entretien e ON e.id_vehicule = a.id_vehicule
WHERE a.date_debut <= COALESCE(e.date_fin, e.date_debut)
  AND e.date_debut <= COALESCE(a.date_fin, e.date_debut);

CREATE OR REPLACE VIEW v_echeance_entretien AS
SELECT v.id_vehicule,
       t.id_type_entretien,
       t.libelle,
       COALESCE(d.date_dernier, v.date_mise_en_service) AS date_dernier,
       COALESCE(d.km_dernier, 0) AS km_dernier,
       COALESCE(d.km_dernier, 0) + t.periodicite_km AS km_prochain,
       (COALESCE(d.date_dernier, v.date_mise_en_service)
           + (t.periodicite_mois || ' months')::interval)::date AS date_prochaine,
       GREATEST(COALESCE(k.km_entretien, 0), COALESCE(ktr.km_trajet, 0),
                COALESCE(kret.km_restitution, 0)) AS km_actuel,
       (GREATEST(COALESCE(k.km_entretien, 0), COALESCE(ktr.km_trajet, 0),
                 COALESCE(kret.km_restitution, 0))
            >= COALESCE(d.km_dernier, 0) + t.periodicite_km
        OR CURRENT_DATE > (COALESCE(d.date_dernier, v.date_mise_en_service)
            + (t.periodicite_mois || ' months')::interval)::date) AS en_retard
FROM vehicule v
CROSS JOIN type_entretien t
LEFT JOIN (
    SELECT id_vehicule, id_type_entretien,
           MAX(date_debut) AS date_dernier, MAX(km_compteur) AS km_dernier
    FROM entretien
    GROUP BY id_vehicule, id_type_entretien
) d ON d.id_vehicule = v.id_vehicule AND d.id_type_entretien = t.id_type_entretien
LEFT JOIN (
    SELECT id_vehicule, MAX(km_compteur) AS km_entretien
    FROM entretien GROUP BY id_vehicule
) k ON k.id_vehicule = v.id_vehicule
LEFT JOIN (
    SELECT a.id_vehicule, MAX(tr.km_arrivee) AS km_trajet
    FROM trajet tr JOIN affectation a ON a.id_affectation = tr.id_affectation
    GROUP BY a.id_vehicule
) ktr ON ktr.id_vehicule = v.id_vehicule
LEFT JOIN (
    SELECT id_vehicule, MAX(km_restitution) AS km_restitution
    FROM affectation GROUP BY id_vehicule
) kret ON kret.id_vehicule = v.id_vehicule
WHERE t.categorie = 'PERIODIQUE';

CREATE OR REPLACE VIEW v_bilan_vehicule AS
SELECT v.id_vehicule,
       v.immatriculation,
       COALESCE(c.cout_total, 0) AS cout_total,
       DATE_PART('year', AGE(CURRENT_DATE, v.date_mise_en_service))::integer AS age_annees,
       GREATEST(COALESCE(k.km_entretien, 0), COALESCE(ktr.km_trajet, 0),
                COALESCE(kret.km_restitution, 0)) AS km_compteur_actuel,
       CASE WHEN GREATEST(COALESCE(k.km_entretien, 0), COALESCE(ktr.km_trajet, 0),
                          COALESCE(kret.km_restitution, 0)) = 0 THEN 0
            ELSE ROUND(COALESCE(c.cout_total, 0)
                 / GREATEST(COALESCE(k.km_entretien, 0), COALESCE(ktr.km_trajet, 0),
                            COALESCE(kret.km_restitution, 0)), 2)
       END AS cout_au_km
FROM vehicule v
LEFT JOIN (
    SELECT id_vehicule, SUM(cout) AS cout_total FROM entretien GROUP BY id_vehicule
) c ON c.id_vehicule = v.id_vehicule
LEFT JOIN (
    SELECT id_vehicule, MAX(km_compteur) AS km_entretien FROM entretien GROUP BY id_vehicule
) k ON k.id_vehicule = v.id_vehicule
LEFT JOIN (
    SELECT a.id_vehicule, MAX(tr.km_arrivee) AS km_trajet
    FROM trajet tr JOIN affectation a ON a.id_affectation = tr.id_affectation
    GROUP BY a.id_vehicule
) ktr ON ktr.id_vehicule = v.id_vehicule
LEFT JOIN (
    SELECT id_vehicule, MAX(km_restitution) AS km_restitution
    FROM affectation GROUP BY id_vehicule
) kret ON kret.id_vehicule = v.id_vehicule;
