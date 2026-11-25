package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Immeuble d'un propriétaire avec ses compteurs (F7).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Immeuble d'un propriétaire avec ses compteurs (F7).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ImmeubleMapper, alimenté par ProprietaireService.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Immeuble d'un propriétaire")
public record ImmeubleProprietaireResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’immeuble
        @Schema(description = "Identifiant", example = "1")
        Integer idImmeuble,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue des Acacias")
        String adresse,
        // Code postal
        @Schema(description = "Code postal", example = "75001")
        String codePostal,
        // Ville
        @Schema(description = "Ville", example = "Paris")
        String ville,
        // Nombre d’étages
        @Schema(description = "Nombre d'étages", example = "3")
        Integer nbEtages,
        // Nombre d’appartements
        @Schema(description = "Nombre d'appartements", example = "6")
        long nbAppartements,
        // Nb baux en cours
        @Schema(description = "Nombre de baux en cours", example = "4")
        long nbBauxEnCours
) {
}
