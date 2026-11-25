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

        @Schema(description = "Identifiant", example = "1")
        Integer idEntretien,

        @Schema(description = "Date de début (D-02)", example = "2026-11-10")
        LocalDate date,

        @Schema(description = "Date de fin de l'immobilisation", example = "2026-11-12")
        LocalDate dateFin,

        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,

        @Schema(description = "Coût", example = "320.00")
        BigDecimal cout,

        @Schema(description = "Kilométrage relevé", example = "19000")
        Integer km,

        @Schema(description = "Type d'entretien", example = "1")
        Integer idTypeEntretien,

        @Schema(description = "Libellé du type", example = "Vidange périodique")
        String libelleType,

        @Schema(description = "Prestataire", example = "1")
        Integer idPrestataire,

        @Schema(description = "Nom du prestataire", example = "Garage Central")
        String nomPrestataire,

        @Schema(description = "Une alerte de conflit a été créée (F12)", example = "false")
        Boolean alerteCreee) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
