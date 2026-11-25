package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : locataire introuvable.
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : locataire introuvable.
 * LANCÉE PAR   : LocataireService, BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 404.
 * POURQUOI     : un identifiant inconnu ne peut pas être confondu avec une donnée vide.
 * NE FAIT PAS  : de logique métier.
 */
public class LocataireNotFoundException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un identifiant donné. */
    public LocataireNotFoundException(Integer idLocataire) {
        super(HttpStatus.NOT_FOUND, "Locataire introuvable", "locataire-introuvable",
                "Aucun locataire ne porte l'identifiant " + idLocataire);
        addProperty("idLocataire", idLocataire);
    }
}
