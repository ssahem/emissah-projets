package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : immeuble introuvable.
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : immeuble introuvable.
 * LANCÉE PAR   : ImmeubleService, AppartementService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 404.
 * POURQUOI     : un identifiant inconnu ne peut pas être confondu avec une donnée vide.
 * NE FAIT PAS  : de logique métier.
 */
public class ImmeubleNotFoundException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un identifiant donné. */
    public ImmeubleNotFoundException(Integer idImmeuble) {
        super(HttpStatus.NOT_FOUND, "Immeuble introuvable", "immeuble-introuvable",
                "Aucun immeuble ne porte l'identifiant " + idImmeuble);
        addProperty("idImmeuble", idImmeuble);
    }
}
