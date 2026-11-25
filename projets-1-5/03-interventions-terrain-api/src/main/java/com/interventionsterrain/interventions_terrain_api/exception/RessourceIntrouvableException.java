package com.interventionsterrain.interventions_terrain_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Erreur métier 404 : ressource absente. Traduite en Problem Detail par le handler global.
 *
 * COUCHE       : Exception
 * RÔLE         : porter le statut HTTP, le titre et le type du Problem Detail correspondant à
 *                la règle violée.
 * LANCÉE PAR   : les services métier.
 * INTERCEPTÉE  : GlobalExceptionHandler.
 * POURQUOI     : signaler un cas métier avec un statut HTTP explicite.
 * NE FAIT PAS  : de logique métier ni de formatage HTTP.
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
