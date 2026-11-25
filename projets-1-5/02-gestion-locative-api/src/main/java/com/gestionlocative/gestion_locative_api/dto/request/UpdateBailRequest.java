package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Corps de modification d'un bail (F5).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un bail (F5).
 * IMMUABLE     : record Java.
 * Un {@link Optional} vide pour dateFin signifie « ne pas modifier » ;
 * {@code Optional.of(...)} avec une date la remplace. Pour retirer une date de
 * fin, le client envoie une date explicitement reportée (F13/F14 gèrent les
 * changements de période). Le loyer nul signifie « ne pas modifier ».
 * UTILISÉ PAR  : BailController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Modification d'un bail")
public record UpdateBailRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Date de fin
        @Schema(description = "Nouvelle date de fin", example = "2027-12-31")
        Optional<LocalDate> dateFin,
        // Loyer mensuel
        @Schema(description = "Nouveau loyer mensuel", example = "900.00")
        @DecimalMin(value = "0.01", message = "Le loyer doit être positif")
        BigDecimal loyerMensuel
) {
}
