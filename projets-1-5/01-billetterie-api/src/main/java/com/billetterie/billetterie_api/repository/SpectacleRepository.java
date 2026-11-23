package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Spectacle;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux spectacles. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface SpectacleRepository extends JpaRepository<Spectacle, Integer> {
    Optional<Spectacle> findFirstByTitreIgnoreCaseAndType(String titre, TypeSpectacle type);
}
