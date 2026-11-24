package com.gestionlocative.gestion_locative_api.mapper;

import com.gestionlocative.gestion_locative_api.dto.response.AppartementLibreResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleAppartementsLibresResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleProprietaireResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleResponse;
import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entité Immeuble et son DTO de sortie.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire Immeuble ↔ ImmeubleResponse ; le nombre d'appartements
 *                est fourni par le service, qui connaît le repository.
 * DÉPEND DE    : l'entité et le DTO uniquement.
 * NE FAIT PAS  : d'accès base, de HTTP, de règles métier.
 */
@Component
public class ImmeubleMapper {

    // ── toResponse() ─────────────────────────────────────────────────────────

    /** Convertit un immeuble en réponse HTTP avec son nombre d'appartements. */
    public ImmeubleResponse toResponse(Immeuble immeuble, long nbAppartements) {
        return new ImmeubleResponse(
                immeuble.getIdImmeuble(), immeuble.getAdresse(), immeuble.getCodePostal(),
                immeuble.getVille(), immeuble.getIdProprietaire(), immeuble.getNbEtages(),
                nbAppartements);
    }

    // ── toProprietaireResponse() ────────────────────────────────────────────

    /** Convertit un immeuble avec les compteurs demandés par F7. */
    public ImmeubleProprietaireResponse toProprietaireResponse(
            Immeuble immeuble, long nbAppartements, long nbBauxEnCours) {
        return new ImmeubleProprietaireResponse(
                immeuble.getIdImmeuble(), immeuble.getAdresse(), immeuble.getCodePostal(),
                immeuble.getVille(), immeuble.getNbEtages(), nbAppartements, nbBauxEnCours);
    }

    // ── toLibresResponse() ──────────────────────────────────────────────────

    /** Regroupe les appartements libres sous leur immeuble (F17). */
    public ImmeubleAppartementsLibresResponse toLibresResponse(
            Immeuble immeuble, List<AppartementLibreResponse> appartements) {
        return new ImmeubleAppartementsLibresResponse(
                immeuble.getIdImmeuble(), immeuble.getAdresse(), immeuble.getVille(),
                appartements);
    }
}
