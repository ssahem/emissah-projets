package com.gestionlocative.gestion_locative_api.dto.response;

import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Bail renvoyé par l'API (F5).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Bail renvoyé par l'API (F5).
 * IMMUABLE     : record Java.
 * Le locataire exposé est le principal (E-08), même si la table de liaison
 * autorise plusieurs locataires.
 * PRODUIT PAR  : BailMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Bail locatif")
public record BailResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du bail
        @Schema(description = "Identifiant", example = "1")
        Integer idBail,
        // Identifiant de l’appartement
        @Schema(description = "Identifiant de l'appartement", example = "1")
        Integer idAppartement,
        // Identifiant du locataire
        @Schema(description = "Identifiant du locataire principal", example = "1")
        Integer idLocataire,
        // Nom du locataire
        @Schema(description = "Nom du locataire principal", example = "Traore")
        String nomLocataire,
        // Prénom du locataire
        @Schema(description = "Prénom du locataire principal", example = "Kofi")
        String prenomLocataire,
        // Date de début
        @Schema(description = "Date de début", example = "2026-01-01")
        LocalDate dateDebut,
        // Date de fin
        @Schema(description = "Date de fin", example = "2026-12-31")
        LocalDate dateFin,
        // Loyer mensuel
        @Schema(description = "Loyer mensuel", example = "850.00")
        BigDecimal loyerMensuel,
        // Provision pour charges
        @Schema(description = "Provision pour charges", example = "50.00")
        BigDecimal provisionCharges,
        // Dépôt de garantie
        @Schema(description = "Dépôt de garantie", example = "900.00")
        BigDecimal depotGarantie,
        // Statut
        @Schema(description = "Statut", example = "EN_COURS")
        StatutBail statut,
        // Date resiliation
        @Schema(description = "Date de résiliation", example = "2026-09-30")
        LocalDate dateResiliation,
        // Date de clôture
        @Schema(description = "Date de clôture", example = "2026-12-31")
        LocalDate dateCloture
) {
}
