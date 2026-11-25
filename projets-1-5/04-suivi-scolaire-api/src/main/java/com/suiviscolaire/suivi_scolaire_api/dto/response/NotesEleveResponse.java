package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Notes d'un élève regroupées dans une matière (F8).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Notes d'un élève regroupées dans une matière (F8).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : NotesEleveMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Notes d'un élève regroupées dans une matière (F8)")
public record NotesEleveResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’élève
        @Schema(description = "Identifiant de l’élève", example = "1")
        Integer idEleve,
        // Nom de l’élève
        @Schema(description = "Nom de l’élève", example = "Traore")
        String nomEleve,
        // Prénom Élève
        @Schema(description = "Prénom Élève", example = "Kofi")
        String prenomEleve,
        // Moyenne
        @Schema(description = "Moyenne", example = "12.34")
        BigDecimal moyenne,
        // Notes
        @Schema(description = "Notes", example = "Exemple")
        List<NoteResponse> notes
) {

}
