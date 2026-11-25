package com.suiviscolaire.suivi_scolaire_api.dto.response;

/**
 * Classe renvoyée par l'API (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Classe renvoyée par l'API (F1).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ClasseService.
 * NE FAIT PAS  : d'accès base.
 */
public record ClasseResponse(
        Integer idClasse, String nom, Integer idAnnee, Integer idNiveau,
        Integer effectifMax, long effectifActuel) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
