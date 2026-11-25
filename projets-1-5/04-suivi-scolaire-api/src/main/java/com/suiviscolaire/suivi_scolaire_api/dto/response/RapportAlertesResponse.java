package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * Rapport de la tâche de détection des difficultés (F16).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Rapport de la tâche de détection des difficultés (F16).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : RapportAlertesMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Rapport de la tâche de détection des difficultés (F16)")
public record RapportAlertesResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Date execution
        @Schema(description = "Date execution", example = "2026-11-24")
        LocalDate dateExecution,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Alertes creees
        @Schema(description = "Alertes creees", example = "Exemple")
        int alertesCreees,
        // Alertes deja presentes
        @Schema(description = "Alertes deja presentes", example = "Exemple")
        int alertesDejaPresentes,
        // Alertes
        @Schema(description = "Alertes", example = "Exemple")
        List<AlerteDifficulteResponse> alertes
) {

}
