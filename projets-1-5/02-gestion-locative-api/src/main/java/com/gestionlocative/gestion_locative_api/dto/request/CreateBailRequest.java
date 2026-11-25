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

        // Appartement
        @Schema(description = "Identifiant de l'appartement", example = "1")
        @NotNull @Positive Integer idAppartement,
        // Locataire principal (E-08)
        @Schema(description = "Identifiant du locataire principal", example = "1")
        @NotNull @Positive Integer idLocataire,
        // Date de début
        @Schema(description = "Date de début", example = "2026-01-01")
        @NotNull LocalDate dateDebut,
        // Date de fin, absente pour un bail sans terme
        @Schema(description = "Date de fin (facultative)", example = "2026-12-31") LocalDate dateFin,
        // Loyer mensuel
        @Schema(description = "Loyer mensuel", example = "850.00")
        @NotNull @DecimalMin(value = "0.01", message = "Le loyer doit être positif")
        BigDecimal loyerMensuel,
        // Provision pour charges, 0 par défaut
        @Schema(description = "Provision pour charges", example = "50.00")
        @DecimalMin(value = "0", message = "La provision ne peut pas être négative")
        BigDecimal provisionCharges,
        // Dépôt de garantie, 0 par défaut
        @Schema(description = "Dépôt de garantie", example = "900.00")
        @DecimalMin(value = "0", message = "Le dépôt ne peut pas être négatif")
        BigDecimal depotGarantie) {
}
