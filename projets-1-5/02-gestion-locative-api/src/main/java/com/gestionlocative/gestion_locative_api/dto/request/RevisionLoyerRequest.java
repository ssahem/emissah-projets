package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Corps d'une révision de loyer (F15).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps d'une révision de loyer (F15).
 * IMMUABLE     : record Java.
 * Le montant doit être strictement positif ; la date d'effet doit être
 * postérieure à la dernière révision et comprise dans la période du bail.
 * UTILISÉ PAR  : BailController, avec @Valid.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Révision du loyer d'un bail")
public record RevisionLoyerRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Montant
        @Schema(description = "Nouveau loyer mensuel", example = "920.00")
        @NotNull
        @DecimalMin(value = "0.01", message = "Le loyer doit être positif")
        BigDecimal montant,
        // Date effet
        @Schema(description = "Date de prise d'effet du nouveau loyer", example = "2027-01-01")
        @NotNull
        LocalDate dateEffet
) {
}
