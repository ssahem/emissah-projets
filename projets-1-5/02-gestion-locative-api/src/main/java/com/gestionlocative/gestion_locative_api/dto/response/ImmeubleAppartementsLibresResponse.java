package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Appartements libres regroupés par immeuble (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Appartements libres regroupés par immeuble (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ImmeubleMapper, alimenté par AppartementService.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Appartements libres d'un immeuble")
public record ImmeubleAppartementsLibresResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’immeuble
        @Schema(description = "Identifiant", example = "1")
        Integer idImmeuble,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue des Acacias")
        String adresse,
        // Ville
        @Schema(description = "Ville", example = "Paris")
        String ville,
        // Appartements
        @Schema(description = "Appartements libres")
        List<AppartementLibreResponse> appartements
) {
}
