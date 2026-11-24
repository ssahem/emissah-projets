package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de réouverture d'un trimestre (F15).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : TrimestreController avec @Valid.
 * NE FAIT PAS  : d'authentification : l'auteur est un texte fourni par l'appelant.
 */
@Schema(description = "Réouverture d'un trimestre")
public record ReouvertureRequest(
        @Schema(description = "Motif d'au moins 20 caractères",
                example = "Correction d'une erreur de saisie sur une note")
        @NotBlank @Size(min = 20) String motif,
        @Schema(description = "Auteur de la réouverture (texte libre)",
                example = "Responsable scolarite") @NotBlank String auteur) {
}
