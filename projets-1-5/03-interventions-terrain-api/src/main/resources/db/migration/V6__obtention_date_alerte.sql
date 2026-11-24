-- S-06 : date de la dernière alerte envoyée pour une compétence proche de l'expiration (F16).
ALTER TABLE obtention_competence ADD COLUMN date_alerte DATE;
