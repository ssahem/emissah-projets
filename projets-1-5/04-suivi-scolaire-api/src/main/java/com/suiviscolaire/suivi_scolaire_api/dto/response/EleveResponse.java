package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Élève renvoyé par l'API, avec sa classe courante (F2, F6, F9).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Élève renvoyé par l'API, avec sa classe courante (F2, F6, F9).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : EleveMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Élève renvoyé par l'API, avec sa classe courante (F2, F6, F9)")
public record EleveResponse(

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
        // Date de naissance
        @Schema(description = "Date de naissance", example = "2026-11-24")
        LocalDate dateNaissance,
        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Nom de la classe
        @Schema(description = "Nom de la classe", example = "6A")
        String nomClasse
) {

}
