package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Une période de l'occupation reconstituée d'un appartement (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Une période de l'occupation reconstituée d'un appartement (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AppartementService, qui découpe les baux selon les révisions.
 * BORNES       : inclusives ; {@code fin} vaut null pour une période encore ouverte.
 * TYPE         : "LOUE" (avec locataire et loyer) ou "VACANT" (sans bail).
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Période d'occupation ou de vacance")
public record PeriodeOccupationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Première date de la période
        @Schema(description = "Début de période", example = "2026-01-01") LocalDate debut,
        // Dernière date de la période, null si elle est ouverte
        @Schema(description = "Fin de période", example = "2026-12-31") LocalDate fin,
        // LOUE ou VACANT
        @Schema(description = "Type de période", example = "LOUE") String type,
        // Bail à l'origine de la période, null pour une vacance
        @Schema(description = "Identifiant du bail", example = "3") Integer idBail,
        // Locataire principal, null pour une vacance
        @Schema(description = "Nom du locataire", example = "Traore") String nomLocataire,
        @Schema(description = "Prénom du locataire", example = "Kofi") String prenomLocataire,
        // Loyer applicable pendant la période, null pour une vacance
        @Schema(description = "Loyer applicable", example = "850.00") BigDecimal loyerMensuel) {
}
