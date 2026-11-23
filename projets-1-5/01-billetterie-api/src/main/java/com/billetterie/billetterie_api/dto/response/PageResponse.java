package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Page générique renvoyée par l'API (F9).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : les services (pagination).
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Page de résultats")
public record PageResponse<T>(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Éléments de la page
        @Schema(description = "Éléments de la page") List<T> content,
        // Numéro de page (0-based)
        @Schema(description = "Numéro de page (0-based)", example = "0") int page,
        // Taille de la page
        @Schema(description = "Taille de la page", example = "20") int size,
        // Nombre total d'éléments
        @Schema(description = "Nombre total d'éléments", example = "57") long totalElements,
        // Nombre total de pages
        @Schema(description = "Nombre total de pages", example = "3") int totalPages) {
}
