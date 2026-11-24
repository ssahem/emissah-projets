package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.time.LocalDate;

/**
 * Élève renvoyé par l'API, avec sa classe courante (F2, F6, F9).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : EleveService, ConsultationService.
 * NE FAIT PAS  : d'accès base.
 */
public record EleveResponse(
        Integer idEleve, String nom, String prenom, LocalDate dateNaissance,
        Integer idClasse, String nomClasse) {
}
