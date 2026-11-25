package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Ligne du classement d'un élève (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Ligne du classement d'un élève (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : LigneClassementMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Ligne du classement d'un élève (F19)")
public record LigneClassementResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’élève
        @Schema(description = "Identifiant de l’élève", example = "1")
        Integer idEleve,
        // Nom
        @Schema(description = "Nom", example = "Traore")
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Kofi")
        String prenom,
        // Identifiant de l’inscription
        @Schema(description = "Identifiant de l’inscription", example = "1")
        Integer idInscription,
        // Moyenne générale
        @Schema(description = "Moyenne générale", example = "12.34")
        BigDecimal moyenneGenerale,
        // Rang
        @Schema(description = "Rang", example = "A")
        Integer rang
) {

}
