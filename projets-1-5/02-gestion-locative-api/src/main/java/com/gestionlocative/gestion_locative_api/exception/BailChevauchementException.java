package com.gestionlocative.gestion_locative_api.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : l'appartement est déjà loué sur la période demandée (F11).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : l'appartement est déjà loué sur la période demandée (F11).
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : le conflit porte sur une donnée existante ; le bail bloquant et
 *                ses dates sont exposés au client, au lieu d'un message SQL brut.
 * NE FAIT PAS  : de logique métier.
 */
public class BailChevauchementException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur avec le bail bloquant et ses dates. */
    public BailChevauchementException(Integer idBailBloquant, LocalDate debut, LocalDate fin) {
        super(HttpStatus.CONFLICT, "Période déjà occupée", "bail-chevauchement",
                "L'appartement est déjà loué sur cette période (bail bloquant n°"
                        + idBailBloquant + ")");
        addProperty("idBailBloquant", idBailBloquant);
        addProperty("debut", debut);
        addProperty("fin", fin);
    }
}
