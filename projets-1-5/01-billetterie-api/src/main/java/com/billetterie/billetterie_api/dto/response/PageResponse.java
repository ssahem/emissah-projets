package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Page générique renvoyée par l'API (F9). COUCHE : DTO (réponse) · IMMUABLE : record. */
@Schema(description = "Page de résultats")
public record PageResponse<T>(
        @Schema(description = "Éléments de la page") List<T> content,
        @Schema(description = "Numéro de page (0-based)", example = "0") int page,
        @Schema(description = "Taille de la page", example = "20") int size,
        @Schema(description = "Nombre total d'éléments", example = "57") long totalElements,
        @Schema(description = "Nombre total de pages", example = "3") int totalPages) {
}
