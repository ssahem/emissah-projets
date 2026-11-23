package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Réservation renvoyée par l'API, avec événement et place (F10). COUCHE : DTO (réponse). */
@Schema(description = "Réservation, événement et place")
public record ReservationResponse(
        @Schema(description = "Identifiant", example = "1") Integer idReservation,
        @Schema(description = "Nom", example = "Dupont") String nom,
        @Schema(description = "Prénom", example = "Jean") String prenom,
        @Schema(description = "Email", example = "jean@example.com") String email,
        @Schema(description = "Identifiant de l'événement", example = "1") Integer idEvenement,
        @Schema(description = "Titre de l'événement", example = "Concert Jazz") String titreEvenement,
        @Schema(description = "Date de l'événement") LocalDateTime dateHeure,
        @Schema(description = "Rang", example = "A") String rang,
        @Schema(description = "Numéro", example = "12") Integer numero,
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie,
        @Schema(description = "Statut courant", example = "PROVISOIRE") String statut,
        @Schema(description = "Date de création") LocalDateTime dateCreation,
        @Schema(description = "Date d'expiration") LocalDateTime dateExpiration,
        @Schema(description = "Montant remboursé", example = "0.00") BigDecimal montantRembourse) {
}
