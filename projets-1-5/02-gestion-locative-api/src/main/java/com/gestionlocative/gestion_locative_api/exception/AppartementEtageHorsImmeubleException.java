package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : étage supérieur au nombre d'étages de l'immeuble (F3).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : AppartementService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : la cohérence étage/immeuble est vérifiée avant l'insertion.
 */
public class AppartementEtageHorsImmeubleException extends ApiException {

    /** Construit l'erreur avec l'étage demandé et le nombre d'étages disponible. */
    public AppartementEtageHorsImmeubleException(Integer etage, Integer nbEtages) {
        super(HttpStatus.BAD_REQUEST, "Étage hors immeuble", "appartement-etage-hors-immeuble",
                "L'étage demandé dépasse le nombre d'étages de l'immeuble");
        addProperty("etage", etage);
        addProperty("nbEtages", nbEtages);
    }
}
