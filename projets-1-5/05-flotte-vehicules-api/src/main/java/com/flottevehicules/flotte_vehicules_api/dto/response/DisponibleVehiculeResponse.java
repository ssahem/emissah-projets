package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Véhicule disponible sur une période (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Véhicule disponible sur une période (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : VehiculeService.
 * NE FAIT PAS  : d'accès base.
 */
@Schema(description = "Véhicule libre avec compteur et prochaine échéance")
public record DisponibleVehiculeResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du véhicule
        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Kilométrage actuel
        @Schema(description = "Kilométrage courant", example = "19000")
        Integer kmActuel,
        // Prochaine echeance
        @Schema(description = "Prochaine échéance d'entretien", example = "2027-10-15")
        LocalDate prochaineEcheance
) {

}
