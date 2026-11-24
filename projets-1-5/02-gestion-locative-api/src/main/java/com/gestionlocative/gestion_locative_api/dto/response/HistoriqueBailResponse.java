package com.gestionlocative.gestion_locative_api.dto.response;

import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Un bail dans l'historique d'un appartement (F8).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : BailMapper, alimenté par AppartementService.
 * LE LOCATAIRE : principal (E-08), comme pour la consultation d'un bail.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Bail de l'historique d'un appartement")
public record HistoriqueBailResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idBail,
        // Locataire principal
        @Schema(description = "Nom du locataire principal", example = "Traore") String nomLocataire,
        @Schema(description = "Prénom du locataire principal", example = "Kofi") String prenomLocataire,
        // Dates
        @Schema(description = "Date de début", example = "2026-01-01") LocalDate dateDebut,
        @Schema(description = "Date de fin", example = "2026-12-31") LocalDate dateFin,
        // Loyer courant
        @Schema(description = "Loyer mensuel courant", example = "850.00") BigDecimal loyerMensuel,
        // Statut du cycle de vie
        @Schema(description = "Statut", example = "EN_COURS") StatutBail statut,
        // Vrai si le bail est actuellement en cours
        @Schema(description = "Bail en cours", example = "true") boolean enCours,
        // Montants antérieurs, hors révision courante
        @Schema(description = "Loyers passés") List<LoyerHistoriqueResponse> loyersPasses) {
}
