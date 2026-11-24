package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Adresse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import org.springframework.stereotype.Component;

/** Conversion Intervention → InterventionResponse ; adresse et responsable résolus en service. */
@Component
public class InterventionMapper {

    public InterventionResponse toResponse(Intervention intervention, Adresse adresse,
                                           Affectation responsable) {
        return new InterventionResponse(
                intervention.getIdIntervention(),
                intervention.getIdClient(),
                intervention.getCodeTypeIntervention(),
                intervention.getDateDemande(),
                intervention.getDebutPrevu(),
                intervention.getFinPrevue(),
                adresse != null ? adresse.getVoie() : null,
                adresse != null ? adresse.getCommune() : null,
                responsable != null ? responsable.getIdTechnicien() : null);
    }
}
