package com.flottevehicules.flotte_vehicules_api.mapper;

import com.flottevehicules.flotte_vehicules_api.dto.response.VehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.repository.SiteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Conversion Vehicule → VehiculeResponse, avec le nom du site et le compteur courant.
 *
 * COUCHE       : Mapper (interface HTTP)
 * DÉPEND DE    : SiteRepository, VueRepository (vue v_bilan_vehicule, lecture seule).
 * NE FAIT PAS  : de logique métier ni de calcul de coût.
 */
@Component
public class VehiculeMapper {

    private final SiteRepository siteRepository;
    private final VueRepository vueRepository;

    public VehiculeMapper(SiteRepository siteRepository, VueRepository vueRepository) {
        this.siteRepository = siteRepository;
        this.vueRepository = vueRepository;
    }

    /** Convertit une entité en réponse enrichie. */
    public VehiculeResponse toResponse(Vehicule vehicule) {
        String nomSite = siteRepository.findById(vehicule.getIdSite())
                .map(Site::getNom)
                .orElse(null);
        Integer kmActuel = vueRepository.bilanVehicule(vehicule.getIdVehicule())
                .map(VueRepository.BilanRow::kmCompteurActuel)
                .orElse(0);
        return new VehiculeResponse(
                vehicule.getIdVehicule(), vehicule.getImmatriculation(),
                vehicule.getMarque(), vehicule.getModele(), vehicule.getDateMiseEnService(),
                vehicule.getModeAcquisition(), vehicule.getIdSite(), nomSite, kmActuel);
    }

    /** Retourne le compteur courant d'un véhicule, zéro s'il est inconnu. */
    public Integer kmActuel(Integer idVehicule) {
        return Optional.ofNullable(idVehicule)
                .flatMap(vueRepository::bilanVehicule)
                .map(VueRepository.BilanRow::kmCompteurActuel)
                .orElse(0);
    }
}
