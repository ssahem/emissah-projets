package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;

/**
 * Matière renvoyée par l'API (F3).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : MatiereService.
 * NE FAIT PAS  : d'accès base.
 */
public record MatiereResponse(
        Integer idMatiere, String libelle, String code, BigDecimal coefficient) {
}
