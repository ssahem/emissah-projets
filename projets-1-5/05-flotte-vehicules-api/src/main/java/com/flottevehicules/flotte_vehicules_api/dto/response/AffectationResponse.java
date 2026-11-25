package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * DTO de sortie d'une affectation (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : DTO de sortie d'une affectation (F4).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AffectationMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Affectation d'un véhicule à un salarié")
public record AffectationResponse(

        @Schema(description = "Identifiant", example = "1")
        Integer idAffectation,

        @Schema(description = "Date de début", example = "2026-11-01")
        LocalDate dateDebut,

        @Schema(description = "Date de fin, nulle si en cours", example = "2026-11-30")
        LocalDate dateFin,

        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,

        @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
        String immatriculation,

        @Schema(description = "Salarié", example = "1")
        Integer idSalarie,

        @Schema(description = "Nom du salarié", example = "Martin")
        String nomSalarie,

        @Schema(description = "Relevé de restitution", example = "19000")
        Integer kmRestitution,

        @Schema(description = "Affectation en cours à la date du jour", example = "true")
        Boolean enCours) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
