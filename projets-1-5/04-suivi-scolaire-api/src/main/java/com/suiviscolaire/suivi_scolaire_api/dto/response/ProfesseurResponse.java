package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.util.List;

/**
 * Professeur renvoyé par l'API, avec ses matières (F4, A-02).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : ProfesseurService.
 * NE FAIT PAS  : d'accès base.
 */
public record ProfesseurResponse(
        Integer idProfesseur, String nom, String prenom, String email, String telephone,
        List<Integer> matieres) {
}
