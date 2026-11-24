package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.time.LocalDate;

/**
 * Résultat d'un changement de classe (F11).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : EleveService.
 * NE FAIT PAS  : de logique métier.
 */
public record ChangementClasseResponse(
        Integer idEleve, Integer ancienneClasse, Integer nouvelleClasse,
        LocalDate dateFinAncienne, LocalDate dateDebutNouvelle, int nombreInscriptions) {
}
