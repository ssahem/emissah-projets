-- ==============================================================================
-- V4__obligatoires.sql — champs obligatoires de l'énoncé (F1, F3, F4)
--
-- V2 ajoute effectif_max, coefficient et email sans NOT NULL : information_schema
-- les déclare NULLABLE, alors que l'énoncé les rend obligatoires. Les données de
-- démonstration ne contiennent aucune valeur nulle pour ces trois colonnes.
-- V2 restant inchangée (checksum Flyway), la contrainte est posée ici.
--
-- Note sur F19 : V1 commente v_rang_classe comme « Rang densifié », mais la vue
-- utilise RANK() et non DENSE_RANK(). Le commentaire de V1 n'est pas modifié pour
-- la même raison de checksum ; le comportement réel est documenté ici et dans
-- l'audit d'affinité. Les ex æquo partagent le même rang et le rang suivant est
-- décalé ; F19 conserve ce comportement et place les élèves sans note en fin de
-- liste sans rang (RestitutionService.classement).
-- ==============================================================================

ALTER TABLE classe ALTER COLUMN effectif_max SET NOT NULL;
ALTER TABLE matiere ALTER COLUMN coefficient SET NOT NULL;
ALTER TABLE professeur ALTER COLUMN email SET NOT NULL;

COMMENT ON VIEW v_rang_classe IS
    'Rang calculé avec RANK() (ex æquo : même rang, rang suivant décalé), pas DENSE_RANK().';
