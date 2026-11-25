package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Corps de création d'un véhicule (F2).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'un véhicule (F2).
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de logique métier ; la date mise en service est contrôlée par le service.
 */
@Schema(description = "Création d'un véhicule ; immatriculation, marque, modèle, date et site requis")
public record CreateVehiculeRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Immatriculation
        @Schema(description = "Immatriculation unique", example = "AB-123-CD")
        @NotBlank(message = "L'immatriculation est obligatoire")
        String immatriculation,
        // Marque
        @Schema(description = "Marque", example = "Peugeot")
        @NotBlank(message = "La marque est obligatoire")
        String marque,
        // Modèle
        @Schema(description = "Modèle", example = "308")
        @NotBlank(message = "Le modèle est obligatoire")
        String modele,
        // Date de mise en service
        @Schema(description = "Date de mise en service, au plus aujourd'hui", example = "2024-01-15")
        @NotNull(message = "La date de mise en service est obligatoire")
        LocalDate dateMiseEnService,
        // Identifiant du site
        @Schema(description = "Identifiant du site courant", example = "1")
        @NotNull(message = "Le site est obligatoire")
        Integer idSite,
        // Mode d’acquisition
        @Schema(description = "Mode d'acquisition : PROPRIETE, LOCATION_LONGUE_DUREE ou LEASING", example = "PROPRIETE")
        String modeAcquisition,
        // Partage
        @Schema(description = "Véhicule partagé", example = "false")
        Boolean partage,
        // Prix achat
        @Schema(description = "Prix d'achat, autorisé seulement en PROPRIETE", example = "25000.00")
        BigDecimal prixAchat
) {

}
