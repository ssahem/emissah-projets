package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Prochaine échéance d'entretien d'un véhicule (F15).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Prochaine échéance d'entretien d'un véhicule (F15).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : VehiculeService.
 * NE FAIT PAS  : d'accès base.
 */
@Schema(description = "Date estimée, kilométrage cible et seuil déclenchant")
public record ProchainEntretienResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du véhicule
        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Libellé du type
        @Schema(description = "Type périodique le plus proche", example = "Vidange périodique")
        String libelleType,
        // Date estimee
        @Schema(description = "Date estimée de l'échéance", example = "2027-10-15")
        LocalDate dateEstimee,
        // Kilométrage cible
        @Schema(description = "Kilométrage cible", example = "34000")
        Integer kmCible,
        // Kilométrage actuel
        @Schema(description = "Kilométrage courant", example = "19000")
        Integer kmActuel,
        // Seuil declencheur
        @Schema(description = "Seuil qui déclenchera : KILOMETRAGE ou DUREE", example = "DUREE")
        String seuilDeclencheur,
        // En retard
        @Schema(description = "Échéance déjà dépassée", example = "false")
        Boolean enRetard
) {

}
