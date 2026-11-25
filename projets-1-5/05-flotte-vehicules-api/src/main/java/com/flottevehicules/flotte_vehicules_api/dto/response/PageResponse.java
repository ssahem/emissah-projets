package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Enveloppe de pagination (F10).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Enveloppe de pagination (F10).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : EntretienService.
 * NE FAIT PAS  : de logique de pagination.
 */
@Schema(description = "Page de résultats avecses métadonnées")
public record PageResponse<T>(

        @Schema(description = "Contenu de la page")
        List<T> content,

        @Schema(description = "Index de la page, zéro basé", example = "0")
        int page,

        @Schema(description = "Taille maximale de la page", example = "20")
        int size,

        @Schema(description = "Nombre total d'éléments", example = "6")
        long totalElements,

        @Schema(description = "Nombre total de pages", example = "1")
        int totalPages) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
