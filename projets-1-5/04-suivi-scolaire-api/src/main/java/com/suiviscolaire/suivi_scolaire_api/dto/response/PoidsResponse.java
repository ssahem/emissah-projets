package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;

/**
 * Poids d'un type d'évaluation (F13).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Poids d'un type d'évaluation (F13).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ReglesMetierService.
 * NE FAIT PAS  : de logique métier.
 */
public record PoidsResponse(String code, String libelle, BigDecimal poids) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
