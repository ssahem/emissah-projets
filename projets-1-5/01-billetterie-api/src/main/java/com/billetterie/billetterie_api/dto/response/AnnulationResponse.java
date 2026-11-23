package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Résultat d'une annulation avec remboursement (F15).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ReservationService.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Annulation d'une réservation et montant remboursé")
public record AnnulationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la réservation
        @Schema(description = "Identifiant de la réservation", example = "1") Integer idReservation,
        // Nouveau statut
        @Schema(description = "Nouveau statut", example = "ANNULEE") String statut,
        // Montant remboursé
        @Schema(description = "Montant remboursé", example = "12.50") BigDecimal montantRembourse,
        // Date de prise d'effet de l'annulation
        @Schema(description = "Date de prise d'effet de l'annulation") LocalDateTime dateAnnulation) {
}
