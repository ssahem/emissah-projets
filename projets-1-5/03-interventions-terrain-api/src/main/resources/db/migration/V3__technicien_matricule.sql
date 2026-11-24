-- E-04 : matricule unique, complété en service à la création du technicien.
ALTER TABLE technicien ADD COLUMN matricule VARCHAR(50);
ALTER TABLE technicien ADD CONSTRAINT uq_technicien_matricule UNIQUE (matricule);
