package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Enveloppe de pagination (F10).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : ConsultationService.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Page de résultats")
public record PageResponse<T>(
        @Schema(description = "Éléments de la page") List<T> content,
        @Schema(description = "Numéro de page (à partir de 0)", example = "0") int page,
        @Schema(description = "Taille de page", example = "30") int size,
        @Schema(description = "Nombre total d'éléments", example = "15") long totalElements,
        @Schema(description = "Nombre total de pages", example = "1") int totalPages) {
}
