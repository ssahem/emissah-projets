package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Regroupement des techniciens disponibles par agence (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Regroupement des techniciens disponibles par agence (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AgenceDisponibleMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Regroupement des techniciens disponibles par agence (F17)")
public record AgenceDisponibleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’agence
        @Schema(description = "Identifiant de l’agence", example = "1")
        Integer idAgence,
        // Nom de l’agence
        @Schema(description = "Nom de l’agence", example = "Traore")
        String nomAgence,
        // Techniciens
        @Schema(description = "Techniciens", example = "Exemple")
        List<TechnicienDisponibleResponse> techniciens
) {
}
