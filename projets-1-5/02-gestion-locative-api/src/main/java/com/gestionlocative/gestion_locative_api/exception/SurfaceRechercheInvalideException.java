package com.gestionlocative.gestion_locative_api.exception;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : surface minimale supérieure à la surface maximale (F9).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : surface minimale supérieure à la surface maximale (F9).
 * LANCÉE PAR   : AppartementService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : un tel critère ne peut jamais produire de résultat ; le refuser
 *                évite de renvoyer une liste vide trompeuse.
 * NE FAIT PAS  : de logique métier.
 */
public class SurfaceRechercheInvalideException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur avec les deux bornes reçues. */
    public SurfaceRechercheInvalideException(BigDecimal surfaceMin, BigDecimal surfaceMax) {
        super(HttpStatus.BAD_REQUEST, "Bornes de surface invalides", "surface-recherche-invalide",
                "La surface minimale ne peut pas dépasser la surface maximale");
        addProperty("surfaceMin", surfaceMin);
        addProperty("surfaceMax", surfaceMax);
    }
}
