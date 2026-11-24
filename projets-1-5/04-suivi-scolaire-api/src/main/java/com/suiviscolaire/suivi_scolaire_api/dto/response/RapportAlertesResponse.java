package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Rapport de la tâche de détection des difficultés (F16).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : AlertesDifficulteService.
 * NE FAIT PAS  : de planification (rôle du scheduler).
 */
public record RapportAlertesResponse(
        LocalDate dateExecution, Integer trimestre, int alertesCreees,
        int alertesDejaPresentes, List<AlerteDifficulteResponse> alertes) {
}
