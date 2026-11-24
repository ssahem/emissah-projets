package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux adresses d'intervention (E-21). */
public interface AdresseRepository extends JpaRepository<Adresse, Integer> {
}
