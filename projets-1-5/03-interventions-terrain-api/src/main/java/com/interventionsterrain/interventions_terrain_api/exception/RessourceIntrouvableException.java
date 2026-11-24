package com.interventionsterrain.interventions_terrain_api.exception;

import org.springframework.http.HttpStatus;

/** Erreur métier 404 : ressource absente. Traduite en Problem Detail par le handler global. */
public class RessourceIntrouvableException extends ApiException {

    public RessourceIntrouvableException(String ressource, Object identifiant) {
        super(HttpStatus.NOT_FOUND, "Ressource introuvable", "ressource-introuvable",
                ressource + " " + identifiant + " introuvable");
        addProperty("ressource", ressource);
        addProperty("identifiant", identifiant);
    }
}
