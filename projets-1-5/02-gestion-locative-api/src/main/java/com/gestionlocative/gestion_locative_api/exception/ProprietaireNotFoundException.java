package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : propriétaire introuvable.
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : propriétaire introuvable.
 * LANCÉE PAR   : ProprietaireService, ImmeubleService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 404.
 * POURQUOI     : un identifiant inconnu ne peut pas être confondu avec une donnée vide.
 * NE FAIT PAS  : de logique métier.
 */
public class ProprietaireNotFoundException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un identifiant donné. */
    public ProprietaireNotFoundException(Integer idProprietaire) {
        super(HttpStatus.NOT_FOUND, "Propriétaire introuvable", "proprietaire-introuvable",
                "Aucun propriétaire ne porte l'identifiant " + idProprietaire);
        addProperty("idProprietaire", idProprietaire);
    }
}
