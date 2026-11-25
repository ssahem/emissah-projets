package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.time.LocalDate;

/**
 * Résultat de la clôture d'un trimestre (F14).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Résultat de la clôture d'un trimestre (F14).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ReglesMetierService.
 * NE FAIT PAS  : de logique métier.
 */
public record ClotureResponse(
        Integer trimestre, LocalDate dateCloture, int nombreEnseignementsVerifies) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
