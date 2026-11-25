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

        @Schema(description = "Identifiant", example = "1")
        Integer idVehicule,

        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,

        @Schema(description = "Marque", example = "Peugeot")
        String marque,

        @Schema(description = "Modèle", example = "308")
        String modele,

        @Schema(description = "Date de mise en service", example = "2024-01-15")
        LocalDate dateMiseEnService,

        @Schema(description = "Mode d'acquisition", example = "PROPRIETE")
        String modeAcquisition,

        @Schema(description = "Identifiant du site courant", example = "1")
        Integer idSite,

        @Schema(description = "Nom du site courant", example = "Paris Centre")
        String nomSite,

        @Schema(description = "Kilométrage courant", example = "19000")
        Integer kmActuel) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
