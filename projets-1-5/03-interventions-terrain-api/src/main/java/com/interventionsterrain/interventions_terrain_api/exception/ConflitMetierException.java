package com.interventionsterrain.interventions_terrain_api.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Erreur métier 409 : état ou unicité incompatible. Le type identifie la règle violée. */
public class ConflitMetierException extends ApiException {

    public ConflitMetierException(String type, String detail) {
        super(HttpStatus.CONFLICT, "Conflit métier", type, detail);
    }

    public ConflitMetierException(String type, String detail, Map<String, Object> properties) {
        this(type, detail);
        properties.forEach(this::addProperty);
    }
}
