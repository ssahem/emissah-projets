package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.HistoriqueStatut;
import com.billetterie.billetterie_api.entity.HistoriqueStatutId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès à l'historique des statuts. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, HistoriqueStatutId> {
    Optional<HistoriqueStatut> findTopByReservation_IdReservationOrderByDateStatutDesc(
            Integer idReservation);

    boolean existsByReservation_IdReservationAndStatut_LibelleIgnoreCase(
            Integer idReservation, String libelle);
}
