package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de modification d'un véhicule (F2) : transfert de site autorisé,
 * immatriculation et date de mise en service figées.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un véhicule (F2) : transfert de site autorisé, immatriculation et date de mise en service figées.
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification du site, de la marque et du modèle ; immatriculation et date figées")
public record UpdateVehiculeRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du site
        @Schema(description = "Nouveau site courant", example = "2")
        @NotNull(message = "Le site est obligatoire")
        Integer idSite,
        // Marque
        @Schema(description = "Marque", example = "Peugeot")
        @NotBlank(message = "La marque est obligatoire")
        String marque,
        // Modèle
        @Schema(description = "Modèle", example = "308")
        @NotBlank(message = "Le modèle est obligatoire")
        String modele
) {

}
