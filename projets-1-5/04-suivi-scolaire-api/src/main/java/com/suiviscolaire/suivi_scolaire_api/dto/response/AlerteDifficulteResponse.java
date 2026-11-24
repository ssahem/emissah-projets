package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Alerte de difficulté renvoyée par l'API (F16).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : AlertesDifficulteService.
 * NE FAIT PAS  : de calcul.
 */
public record AlerteDifficulteResponse(
        Integer idAlerte, Integer idInscription, Integer idEleve, String nomEleve,
        Integer idClasse, String nomClasse, Integer trimestre, BigDecimal moyenne,
        BigDecimal ecart, String motif, LocalDate dateAlerte) {
}
