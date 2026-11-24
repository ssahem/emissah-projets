package com.flottevehicules.flotte_vehicules_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Ressource introuvable par son identifiant.
 *
 * COUCHE       : Exception
 * LANCÉE PAR   : les services de la flotte.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 404.
 * POURQUOI     : distinguer une ressource absente d'une donnée invalide.
 */
public class RessourceIntrouvableException extends ApiException {

    public RessourceIntrouvableException(String ressource, Object identifiant) {
        super(HttpStatus.NOT_FOUND, "Ressource introuvable", "ressource-introuvable",
                ressource + " introuvable pour l'identifiant " + identifiant);
        addProperty("ressource", ressource);
        addProperty("identifiant", identifiant);
    }
}
