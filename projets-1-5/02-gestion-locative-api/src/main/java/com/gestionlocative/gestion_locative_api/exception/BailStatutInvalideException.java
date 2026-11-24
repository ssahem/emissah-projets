package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : l'état du bail n'autorise pas l'action demandée (F13, F14).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : le conflit porte sur l'état courant (déjà résilié, terminé,
 *                non commencé ou sans échéance), pas sur le format de la requête.
 */
public class BailStatutInvalideException extends ApiException {

    /** Construit l'erreur avec le bail concerné et la raison du refus. */
    public BailStatutInvalideException(Integer idBail, String raison) {
        super(HttpStatus.CONFLICT, "Action impossible sur ce bail", "bail-statut-invalide",
                "Le bail n°" + idBail + " ne permet pas cette action : " + raison);
        addProperty("idBail", idBail);
        addProperty("raison", raison);
    }
}
