package com.interventionsterrain.interventions_terrain_api.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Erreur métier 409 : état ou unicité incompatible. Le type identifie la règle violée.
 *
 * COUCHE       : Exception
 * RÔLE         : porter le statut HTTP, le titre et le type du Problem Detail correspondant à
 *                la règle violée.
 * LANCÉE PAR   : les services métier.
 * INTERCEPTÉE  : GlobalExceptionHandler.
 * POURQUOI     : signaler un cas métier avec un statut HTTP explicite.
 * NE FAIT PAS  : de logique métier ni de formatage HTTP.
 */
public class ConflitMetierException extends ApiException {

    public ConflitMetierException(String type, String detail) {
        super(HttpStatus.CONFLICT, "Conflit métier", type, detail);
    }

    public ConflitMetierException(String type, String detail, Map<String, Object> properties) {
        this(type, detail);
        properties.forEach(this::addProperty);
    }
}
