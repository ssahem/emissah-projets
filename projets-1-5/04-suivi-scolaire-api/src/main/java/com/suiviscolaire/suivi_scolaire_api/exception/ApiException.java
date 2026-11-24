package com.suiviscolaire.suivi_scolaire_api.exception;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Base commune des erreurs métier de l'API de suivi scolaire.
 *
 * COUCHE       : Exception
 * RÔLE         : porter le statut, le titre, le type et les données utiles au traducteur
 *                global.
 * INTERCEPTÉE  : par GlobalExceptionHandler → Problem Details (RFC 9457).
 * NE FAIT PAS  : de logique métier ni de formatage HTTP.
 */
public abstract class ApiException extends RuntimeException {

    private static final String BASE = "https://api.suivi-scolaire.local/problems/";

    private final HttpStatus status;
    private final String title;
    private final URI type;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    protected ApiException(HttpStatus status, String title, String type, String detail) {
        super(detail);
        this.status = status;
        this.title = title;
        this.type = URI.create(BASE + type);
    }

    /** Retourne le statut HTTP associé. */
    public HttpStatus getStatus() { return status; }

    /** Retourne le titre lisible du problème. */
    public String getTitle() { return title; }

    /** Retourne l'identifiant URI du type de problème. */
    public URI getType() { return type; }

    /** Retourne les données complémentaires exposées au client. */
    public Map<String, Object> getProperties() { return Map.copyOf(properties); }

    /** Ajoute une donnée complémentaire au problème. */
    protected void addProperty(String key, Object value) { properties.put(key, value); }
}
