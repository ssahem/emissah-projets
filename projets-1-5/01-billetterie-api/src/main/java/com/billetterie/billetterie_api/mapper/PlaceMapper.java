package com.billetterie.billetterie_api.mapper;

import com.billetterie.billetterie_api.dto.response.PlaceResponse;
import com.billetterie.billetterie_api.dto.response.PlaceSalleResponse;
import com.billetterie.billetterie_api.entity.Place;
import org.springframework.stereotype.Component;

/**
 * Conversions des places en réponses HTTP.
 *
 * COUCHE       : Mapper
 * RÔLE         : produire les DTO des endpoints F2, F5 et F6.
 * NE FAIT PAS  : de gestion du cycle de vie des places.
 */
@Component
public class PlaceMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit une place en réponse HTTP.
     */

    public PlaceSalleResponse toResponse(Place place) {
        return new PlaceSalleResponse(
                place.getIdPlace(), place.getRang(), place.getNumero(),
                place.getCategorie().getLibelle());
    }

    // ── toPlaceResponse() ────────────────────────────────────────────────────

    /**
     * Convertit une place en réponse complète pour le CRUD (F2).
     */

    public PlaceResponse toPlaceResponse(Place place) {
        return new PlaceResponse(
                place.getIdPlace(), place.getSalle().getIdSalle(), place.getRang(),
                place.getNumero(), place.getCategorie().getLibelle());
    }
}
