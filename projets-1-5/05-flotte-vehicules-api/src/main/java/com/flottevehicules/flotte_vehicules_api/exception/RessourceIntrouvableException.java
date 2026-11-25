package com.flottevehicules.flotte_vehicules_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Ressource introuvable par son identifiant.
 *
 * COUCHE       : Exception
 * RÔLE         : Ressource introuvable par son identifiant.
 * LANCÉE PAR   : les services de la flotte.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 404.
 * POURQUOI     : distinguer une ressource absente d'une donnée invalide.
 * NE FAIT PAS  : de logique métier.
 */
public class RessourceIntrouvableException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    public RessourceIntrouvableException(String ressource, Object identifiant) {
        super(HttpStatus.NOT_FOUND, "Ressource introuvable", "ressource-introuvable",
                ressource + " introuvable pour l'identifiant " + identifiant);
        addProperty("ressource", ressource);
        addProperty("identifiant", identifiant);
    }
}
