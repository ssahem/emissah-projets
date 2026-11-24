package com.gestionlocative.gestion_locative_api.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : date de fin incohérente avec la date de début (F12).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : ck_bail_dates est conservé (E-03), mais un 400 explicite vaut
 *                mieux qu'une violation de contrainte SQL.
 */
public class BailDateFinInvalideException extends ApiException {

    /** Construit l'erreur avec les deux dates en conflit. */
    public BailDateFinInvalideException(LocalDate dateDebut, LocalDate dateFin) {
        super(HttpStatus.BAD_REQUEST, "Date de fin invalide", "bail-date-fin-invalide",
                "La date de fin doit être postérieure à la date de début et au moins un mois après");
        addProperty("dateDebut", dateDebut);
        addProperty("dateFin", dateFin);
    }
}
