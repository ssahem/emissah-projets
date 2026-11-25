package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : locataire titulaire d'un bail en cours (F4).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : locataire titulaire d'un bail en cours (F4).
 * LANCÉE PAR   : LocataireService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : la suppression effacerait la liaison avec un bail actif.
 * NE FAIT PAS  : de logique métier.
 */
public class LocataireNonSupprimableException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un identifiant donné. */
    public LocataireNonSupprimableException(Integer idLocataire) {
        super(HttpStatus.CONFLICT, "Locataire non supprimable", "locataire-non-supprimable",
                "Ce locataire est encore titulaire d'un bail en cours");
        addProperty("idLocataire", idLocataire);
    }
}
