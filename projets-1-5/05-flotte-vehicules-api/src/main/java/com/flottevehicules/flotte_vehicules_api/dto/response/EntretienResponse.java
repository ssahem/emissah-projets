package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de sortie d'un entretien (F5, F8, F10).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : DTO de sortie d'un entretien (F5, F8, F10).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : EntretienMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Entretien d'un véhicule avec son coût")
public record EntretienResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’entretien
        @Schema(description = "Identifiant", example = "1")
        Integer idEntretien,
        // Date
        @Schema(description = "Date", example = "2026-11-24")
        LocalDate date,
        // Date de fin
        @Schema(description = "Date de fin de l'immobilisation", example = "2026-11-12")
        LocalDate dateFin,
        // Identifiant du véhicule
        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,
        // Coût
        @Schema(description = "Coût", example = "320.00")
        BigDecimal cout,
        // Kilométrage
        @Schema(description = "Kilométrage relevé", example = "19000")
        Integer km,
        // Identifiant du type d’entretien
        @Schema(description = "Type d'entretien", example = "1")
        Integer idTypeEntretien,
        // Libellé du type
        @Schema(description = "Libellé du type", example = "Vidange périodique")
        String libelleType,
        // Identifiant du prestataire
        @Schema(description = "Prestataire", example = "1")
        Integer idPrestataire,
        // Nom Prestataire
        @Schema(description = "Nom du prestataire", example = "Garage Central")
        String nomPrestataire,
        // Alerte creee
        @Schema(description = "Alerte creee", example = "true")
        Boolean alerteCreee
) {

}
