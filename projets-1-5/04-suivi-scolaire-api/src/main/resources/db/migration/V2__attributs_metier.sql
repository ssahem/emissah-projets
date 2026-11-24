-- ==============================================================================
-- V2__attributs_metier.sql — attributs et contraintes simples de l'énoncé
--
-- Ajoute uniquement ce qui n'existe pas dans le schéma fourni. Les contraintes
-- déjà présentes en V1 (unicité libellé/code, poids, valeur de note, nom de
-- classe par année, numéro d'inscription) ne sont pas dupliquées.
-- ==============================================================================

-- F1/F6 : capacité d'accueil et unicité du nom dans un même niveau.
ALTER TABLE classe ADD COLUMN effectif_max integer;
ALTER TABLE classe ADD CONSTRAINT classe_effectif_max_check CHECK (effectif_max > 0);
ALTER TABLE classe ADD CONSTRAINT uq_classe_nom_niveau UNIQUE (nom, id_niveau);

-- F2/F6/F11 : date de fin pour conserver l'ancienne affectation d'un élève.
ALTER TABLE inscription ADD COLUMN date_fin date;

-- F3/F18 : coefficient global de la matière (distinct de la table coefficient par niveau).
ALTER TABLE matiere ADD COLUMN coefficient numeric;
ALTER TABLE matiere ADD CONSTRAINT matiere_coefficient_check CHECK (coefficient > 0);

-- La moyenne générale utilise désormais le coefficient global de la matière (F18).
CREATE OR REPLACE VIEW v_moyenne_generale AS
SELECT vm.id_inscription,
       vm.numero_trimestre,
       ROUND(SUM(vm.moyenne_matiere * m.coefficient) / NULLIF(SUM(m.coefficient), 0), 2) AS moyenne_generale
FROM v_moyenne_matiere vm
JOIN matiere m ON m.id_matiere = vm.id_matiere
GROUP BY vm.id_inscription, vm.numero_trimestre;

-- F4 : coordonnées du professeur.
ALTER TABLE professeur ADD COLUMN email character varying;
ALTER TABLE professeur ADD CONSTRAINT professeur_email_key UNIQUE (email);
ALTER TABLE professeur ADD COLUMN telephone character varying;

-- F5/F13/F14/F15 : état de clôture du trimestre.
ALTER TABLE trimestre ADD COLUMN est_cloture boolean NOT NULL DEFAULT false;
ALTER TABLE trimestre ADD COLUMN date_cloture date;
