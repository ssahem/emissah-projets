package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Corps de création d'un bail (F5).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'un bail (F5).
 * IMMUABLE     : record Java.
 * La provision et le dépôt sont facultatifs : l'énoncé ne les rend pas
 * obligatoires, la base les impose NOT NULL (le service applique 0 par défaut).
 * UTILISÉ PAR  : BailController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'un bail")
public record CreateBailRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’appartement
        @Schema(description = "Identifiant de l'appartement", example = "1")
        @NotNull
        @Positive
        Integer idAppartement,
        // Identifiant du locataire
        @Schema(description = "Identifiant du locataire principal", example = "1")
        @NotNull
        @Positive
        Integer idLocataire,
        // Date de début
        @Schema(description = "Date de début", example = "2026-01-01")
        @NotNull
        LocalDate dateDebut,
        // Date de fin
        @Schema(description = "Date de fin", example = "2026-11-24")
        LocalDate dateFin,
        // Loyer mensuel
        @Schema(description = "Loyer mensuel", example = "850.00")
        @NotNull
        @DecimalMin(value = "0.01", message = "Le loyer doit être positif")
        BigDecimal loyerMensuel,
        // Provision pour charges
        @Schema(description = "Provision pour charges", example = "50.00")
        @DecimalMin(value = "0", message = "La provision ne peut pas être négative")
        BigDecimal provisionCharges,
        // Dépôt de garantie
        @Schema(description = "Dépôt de garantie", example = "900.00")
        @DecimalMin(value = "0", message = "Le dépôt ne peut pas être négatif")
        BigDecimal depotGarantie
) {
}
