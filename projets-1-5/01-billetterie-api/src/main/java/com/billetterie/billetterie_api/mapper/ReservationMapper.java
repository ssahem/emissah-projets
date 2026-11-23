package com.billetterie.billetterie_api.mapper;

import com.billetterie.billetterie_api.dto.response.ReservationResponse;
import com.billetterie.billetterie_api.entity.Reservation;
import com.billetterie.billetterie_api.entity.ReservationPlace;
import org.springframework.stereotype.Component;

/**
 * Conversions des réservations en réponses HTTP.
 *
 * COUCHE       : Mapper
 * RÔLE         : assembler réservation, événement, place et statut courant.
 * NE FAIT PAS  : de calcul de statut (fourni par le service).
 */
@Component
public class ReservationMapper {

    // ── toResponse() ─────────────────────────────────────────────────────────

    /** Convertit une réservation en réponse HTTP. */
    public ReservationResponse toResponse(Reservation reservation, String statut, String categorie) {
        ReservationPlace place = reservation.getPlaces().stream().findFirst().orElse(null);
        return new ReservationResponse(
                reservation.getIdReservation(),
                reservation.getClient().getNom(),
                reservation.getClient().getPrenom(),
                reservation.getClient().getEmail(),
                reservation.getEvenement().getIdEvenement(),
                reservation.getEvenement().getSpectacle().getTitre(),
                reservation.getEvenement().getDateHeure(),
                place == null ? null : place.getRang(),
                place == null ? null : place.getNumero(),
                categorie,
                statut,
                reservation.getDateCreation(),
                reservation.getDateExpiration(),
                reservation.getMontantRembourse());
    }
}
