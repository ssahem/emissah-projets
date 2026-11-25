package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Résultat d'un changement de classe (F11).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Résultat d'un changement de classe (F11).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ChangementClasseMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Résultat d'un changement de classe (F11)")
public record ChangementClasseResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’élève
        @Schema(description = "Identifiant de l’élève", example = "1")
        Integer idEleve,
        // Ancienne Classe
        @Schema(description = "Ancienne Classe", example = "1")
        Integer ancienneClasse,
        // Nouvelle Classe
        @Schema(description = "Nouvelle Classe", example = "1")
        Integer nouvelleClasse,
        // Date de fin ancienne
        @Schema(description = "Date de fin ancienne", example = "2026-11-24")
        LocalDate dateFinAncienne,
        // Date de début nouvelle
        @Schema(description = "Date de début nouvelle", example = "2026-11-24")
        LocalDate dateDebutNouvelle,
        // Nombre inscriptions
        @Schema(description = "Nombre inscriptions", example = "Traore")
        int nombreInscriptions
) {

}
