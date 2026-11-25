package com.gestionlocative.gestion_locative_api.mapper;

import com.gestionlocative.gestion_locative_api.dto.response.ProprietaireResponse;
import com.gestionlocative.gestion_locative_api.entity.Proprietaire;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entité Proprietaire et son DTO de sortie.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire Proprietaire ↔ ProprietaireResponse sans logique métier.
 * DÉPEND DE    : l'entité et le DTO uniquement.
 * NE FAIT PAS  : d'accès base, de HTTP, de règles métier.
 */
@Component
public class ProprietaireMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /** Convertit un propriétaire en réponse HTTP. */
    public ProprietaireResponse toResponse(Proprietaire proprietaire) {
        return new ProprietaireResponse(
                proprietaire.getIdProprietaire(), proprietaire.getNom(), proprietaire.getPrenom(),
                proprietaire.getEmail(), proprietaire.getTelephone());
    }
}
