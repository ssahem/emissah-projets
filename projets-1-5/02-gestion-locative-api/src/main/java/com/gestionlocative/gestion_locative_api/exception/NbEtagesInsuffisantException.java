package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : nombre d'étages inférieur à l'étage maximum occupé (F2).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : nombre d'étages inférieur à l'étage maximum occupé (F2).
 * LANCÉE PAR   : ImmeubleService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : réduire le nombre d'étages rendrait des appartements incohérents.
 * NE FAIT PAS  : de logique métier.
 */
public class NbEtagesInsuffisantException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur avec l'étage maximum encore enregistré. */
    public NbEtagesInsuffisantException(Integer idImmeuble, Integer etageMax) {
        super(HttpStatus.BAD_REQUEST, "Nombre d'étages insuffisant", "nb-etages-insuffisant",
                "Le nombre d'étages ne peut pas être inférieur à l'étage le plus élevé des appartements ("
                        + etageMax + ")");
        addProperty("idImmeuble", idImmeuble);
        addProperty("etageMax", etageMax);
    }
}
