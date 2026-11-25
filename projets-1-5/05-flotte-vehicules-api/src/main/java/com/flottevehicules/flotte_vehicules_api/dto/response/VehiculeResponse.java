package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * DTO de sortie d'un véhicule, avec son site et son compteur courant (F2).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : DTO de sortie d'un véhicule, avec son site et son compteur courant (F2).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : VehiculeMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Véhicule du parc avec son site courant")
public record VehiculeResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du véhicule
        @Schema(description = "Identifiant", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Marque
        @Schema(description = "Marque", example = "Peugeot")
        String marque,
        // Modèle
        @Schema(description = "Modèle", example = "308")
        String modele,
        // Date de mise en service
        @Schema(description = "Date de mise en service", example = "2024-01-15")
        LocalDate dateMiseEnService,
        // Mode d’acquisition
        @Schema(description = "Mode d'acquisition", example = "PROPRIETE")
        String modeAcquisition,
        // Identifiant du site
        @Schema(description = "Identifiant du site courant", example = "1")
        Integer idSite,
        // Nom du site courant
        @Schema(description = "Nom du site courant", example = "Paris Centre")
        String nomSite,
        // Kilométrage actuel
        @Schema(description = "Kilométrage courant", example = "19000")
        Integer kmActuel
) {

}
