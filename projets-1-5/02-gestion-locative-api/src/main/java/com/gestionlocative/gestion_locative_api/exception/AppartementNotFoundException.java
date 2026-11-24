package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : appartement introuvable.
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : AppartementService, BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 404.
 * POURQUOI     : un identifiant inconnu ne peut pas être confondu avec une donnée vide.
 */
public class AppartementNotFoundException extends ApiException {

    /** Construit l'erreur pour un identifiant donné. */
    public AppartementNotFoundException(Integer idAppartement) {
        super(HttpStatus.NOT_FOUND, "Appartement introuvable", "appartement-introuvable",
                "Aucun appartement ne porte l'identifiant " + idAppartement);
        addProperty("idAppartement", idAppartement);
    }
}
