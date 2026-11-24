package com.gestionlocative.gestion_locative_api.mapper;

import com.gestionlocative.gestion_locative_api.dto.response.LocataireResponse;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entité Locataire et son DTO de sortie.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire Locataire ↔ LocataireResponse sans logique métier.
 * DÉPEND DE    : l'entité et le DTO uniquement.
 * NE FAIT PAS  : d'accès base, de HTTP, de règles métier.
 */
@Component
public class LocataireMapper {

    // ── toResponse() ─────────────────────────────────────────────────────────

    /** Convertit un locataire en réponse HTTP. */
    public LocataireResponse toResponse(Locataire locataire) {
        return new LocataireResponse(
                locataire.getIdLocataire(), locataire.getNom(), locataire.getPrenom(),
                locataire.getEmail(), locataire.getTelephone());
    }
}
