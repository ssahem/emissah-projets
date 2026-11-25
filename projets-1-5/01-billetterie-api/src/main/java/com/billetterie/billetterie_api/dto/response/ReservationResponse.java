package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Réservation renvoyée par l'API, avec événement et place (F10).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Réservation renvoyée par l'API, avec événement et place (F10).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ReservationMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Réservation, événement et place")
public record ReservationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idReservation,
        // Nom
        @Schema(description = "Nom", example = "Dupont") String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Jean") String prenom,
        // Email
        @Schema(description = "Email", example = "jean@example.com") String email,
        // Identifiant de l'événement
        @Schema(description = "Identifiant de l'événement", example = "1") Integer idEvenement,
        // Titre de l'événement
        @Schema(description = "Titre de l'événement", example = "Concert Jazz") String titreEvenement,
        // Date de l'événement
        @Schema(description = "Date de l'événement") LocalDateTime dateHeure,
        // Rang
        @Schema(description = "Rang", example = "A") String rang,
        // Numéro
        @Schema(description = "Numéro", example = "12") Integer numero,
        // Catégorie
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie,
        // Statut courant
        @Schema(description = "Statut courant", example = "PROVISOIRE") String statut,
        // Date de création
        @Schema(description = "Date de création") LocalDateTime dateCreation,
        // Date d'expiration
        @Schema(description = "Date d'expiration") LocalDateTime dateExpiration,
        // Montant remboursé
        @Schema(description = "Montant remboursé", example = "0.00") BigDecimal montantRembourse) {
}
