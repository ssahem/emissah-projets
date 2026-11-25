package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.time.LocalDate;

/**
 * Trace de réouverture renvoyée par l'API (F15).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Trace de réouverture renvoyée par l'API (F15).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ReglesMetierService.
 * NE FAIT PAS  : d'authentification.
 */
public record ReouvertureResponse(
        Integer idHistorique, Integer trimestre, LocalDate dateReouverture,
        String motif, String auteur) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
