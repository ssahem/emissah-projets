package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : propriétaire encore rattaché à des immeubles (F1).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : ProprietaireService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : la suppression laisserait des immeubles orphelins.
 */
public class ProprietaireNonSupprimableException extends ApiException {

    /** Construit l'erreur pour un identifiant donné. */
    public ProprietaireNonSupprimableException(Integer idProprietaire) {
        super(HttpStatus.CONFLICT, "Propriétaire non supprimable", "proprietaire-non-supprimable",
                "Ce propriétaire possède encore des immeubles");
        addProperty("idProprietaire", idProprietaire);
    }
}
