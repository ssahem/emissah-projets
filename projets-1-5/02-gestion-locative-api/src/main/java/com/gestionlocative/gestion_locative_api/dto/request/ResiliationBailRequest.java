package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Corps d'une résiliation anticipée (F13).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps d'une résiliation anticipée (F13).
 * IMMUABLE     : record Java.
 * La date d'effet doit être comprise dans la période du bail et ne pas être
 * antérieure à aujourd'hui ; ces règles sont vérifiées par le service.
 * UTILISÉ PAR  : BailController, avec @Valid.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Résiliation anticipée d'un bail")
public record ResiliationBailRequest(

        // ═════════════════════════════════════════════════════════════════════
        //  CHAMPS
        // ═════════════════════════════════════════════════════════════════════

        @Schema(description = "Date de prise d'effet de la résiliation", example = "2026-09-30")
        @NotNull LocalDate dateEffet) {
}
