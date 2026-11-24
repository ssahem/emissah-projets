package com.interventionsterrain.interventions_terrain_api.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Erreur métier 400 : donnée ou référence invalide, à distinguer du conflit de données. */
public class DonneeInvalideException extends ApiException {

    public DonneeInvalideException(String type, String detail) {
        super(HttpStatus.BAD_REQUEST, "Donnée invalide", type, detail);
    }

    public DonneeInvalideException(String type, String detail, Map<String, Object> properties) {
        this(type, detail);
        properties.forEach(this::addProperty);
    }
}
