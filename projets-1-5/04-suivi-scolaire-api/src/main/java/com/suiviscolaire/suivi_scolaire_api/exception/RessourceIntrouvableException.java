package com.suiviscolaire.suivi_scolaire_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Erreur métier 404 : ressource absente.
 *
 * COUCHE       : Exception
 * RÔLE         : signaler une ressource inconnue avec son identifiant.
 * LANCÉE PAR   : les services métier.
 * INTERCEPTÉE  : GlobalExceptionHandler → 404 Problem Details.
 * POURQUOI     : exprimer l'erreur en vocabulaire métier, pas en HTTP.
 * NE FAIT PAS  : de logique métier.
 */
public class RessourceIntrouvableException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    public RessourceIntrouvableException(String ressource, Object identifiant) {
        super(HttpStatus.NOT_FOUND, "Ressource introuvable", "ressource-introuvable",
                ressource + " " + identifiant + " introuvable");
        addProperty("ressource", ressource);
        addProperty("identifiant", identifiant);
    }
}
