package com.gestionlocative.gestion_locative_api.exception;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Base commune des erreurs métier de la gestion locative.
 *
 * COUCHE       : Exception
 * RÔLE         : porter le statut HTTP, le titre, le type et les données utiles
 *                nécessaires au traducteur global.
 * INTERCEPTÉE  : par GlobalExceptionHandler → Problem Details (RFC 9457).
 * NE FAIT PAS  : de logique métier ni de formatage HTTP.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String title;
    private final URI type;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    protected ApiException(HttpStatus status, String title, String type, String detail) {
        super(detail);
        this.status = status;
        this.title = title;
        this.type = URI.create("https://api.gestion-locative.local/problems/" + type);
    }

    /** Retourne le statut HTTP associé. */
    public HttpStatus getStatus() { return status; }

    /** Retourne le titre lisible du problème. */
    public String getTitle() { return title; }

    /** Retourne l'identifiant URI du type de problème. */
    public URI getType() { return type; }

    /** Retourne les données complémentaires à exposer au client. */
    public Map<String, Object> getProperties() { return Map.copyOf(properties); }

    /** Ajoute une donnée complémentaire au problème. */
    protected void addProperty(String key, Object value) { properties.put(key, value); }
}
