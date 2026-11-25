package com.suiviscolaire.suivi_scolaire_api.dto.response;

/**
 * Résultat d'une affectation de matière à un professeur (F12).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Résultat d'une affectation de matière à un professeur (F12).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ProfesseurService.
 * NE FAIT PAS  : de logique métier.
 */
public record AffectationResponse(
        Integer idEnseignement, Integer idProfesseur, Integer idClasse, Integer idMatiere) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}
