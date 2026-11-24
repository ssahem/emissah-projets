package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : bail introuvable.
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 404.
 * POURQUOI     : un identifiant inconnu ne peut pas être confondu avec une donnée vide.
 */
public class BailNotFoundException extends ApiException {

    /** Construit l'erreur pour un identifiant donné. */
    public BailNotFoundException(Integer idBail) {
        super(HttpStatus.NOT_FOUND, "Bail introuvable", "bail-introuvable",
                "Aucun bail ne porte l'identifiant " + idBail);
        addProperty("idBail", idBail);
    }
}
