package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un montant de loyer passé, issu de revision_loyer (F8).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : BailMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Loyer passé d'un bail")
public record LoyerHistoriqueResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Date de prise d'effet
        @Schema(description = "Date d'effet", example = "2026-01-01") LocalDate dateEffet,
        // Montant
        @Schema(description = "Montant", example = "850.00") BigDecimal montant) {
}
