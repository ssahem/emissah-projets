package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Résultat d'une annulation avec remboursement (F15). COUCHE : DTO (réponse). */
@Schema(description = "Annulation d'une réservation et montant remboursé")
public record AnnulationResponse(
        @Schema(description = "Identifiant de la réservation", example = "1") Integer idReservation,
        @Schema(description = "Nouveau statut", example = "ANNULEE") String statut,
        @Schema(description = "Montant remboursé", example = "12.50") BigDecimal montantRembourse,
        @Schema(description = "Date de prise d'effet de l'annulation") LocalDateTime dateAnnulation) {
}
