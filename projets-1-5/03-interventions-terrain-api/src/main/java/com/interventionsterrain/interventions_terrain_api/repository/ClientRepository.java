package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Client;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux clients (F4). */
public interface ClientRepository extends JpaRepository<Client, Integer> {

    List<Client> findAllByOrderByNomAsc();

    boolean existsByEmail(String email);
}
