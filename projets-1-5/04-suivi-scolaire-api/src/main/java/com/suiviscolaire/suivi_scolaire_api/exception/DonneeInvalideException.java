package com.suiviscolaire.suivi_scolaire_api.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Erreur métier 400 : donnée ou référence invalide.
 *
 * COUCHE       : Exception
 * RÔLE         : signaler une règle applicative violée (trimestre inconnu, motif court…).
 * LANCÉE PAR   : les services métier.
 * INTERCEPTÉE  : GlobalExceptionHandler → 400 Problem Details.
 * NE FAIT PAS  : de logique métier.
 */
public class DonneeInvalideException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    public DonneeInvalideException(String type, String detail) {
        super(HttpStatus.BAD_REQUEST, "Donnée invalide", type, detail);
    }

    public DonneeInvalideException(String type, String detail, Map<String, Object> properties) {
        this(type, detail);
        properties.forEach(this::addProperty);
    }
}
