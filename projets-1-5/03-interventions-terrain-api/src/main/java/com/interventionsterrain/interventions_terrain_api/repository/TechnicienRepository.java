package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux techniciens (F2). */
public interface TechnicienRepository extends JpaRepository<Technicien, Integer> {

    List<Technicien> findAllByOrderByNomAsc();

    Optional<Technicien> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    boolean existsByEmail(String email);

    List<Technicien> findAllByIdEquipeInOrderByNomAsc(List<Integer> idsEquipe);

    /** Verrou pessimiste pour sérialiser les contrôles de chevauchement (F12, S-08). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Technicien t where t.idTechnicien = :idTechnicien")
    Optional<Technicien> findByIdForUpdate(@Param("idTechnicien") Integer idTechnicien);
}
