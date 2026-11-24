package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.util.List;

/**
 * Effectif et élèves d'une classe (F6).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : ConsultationService.
 * NE FAIT PAS  : de calcul métier.
 */
public record EffectifClasseResponse(
        Integer idClasse, String nomClasse, long effectifActuel, Integer effectifMax,
        List<EleveResponse> eleves) {
}
