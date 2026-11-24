package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Corps d'un renouvellement de bail (F14).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * La nouvelle date de fin doit être postérieure à l'échéance courante et ne
 * pas chevaucher un autre bail ; ces règles sont vérifiées par le service.
 * UTILISÉ PAR  : BailController, avec @Valid.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Renouvellement d'un bail")
public record RenouvellementBailRequest(

        // ═════════════════════════════════════════════════════════════════════
        //  CHAMPS
        // ═════════════════════════════════════════════════════════════════════

        @Schema(description = "Nouvelle date de fin", example = "2028-06-30")
        @NotNull LocalDate dateFin) {
}
