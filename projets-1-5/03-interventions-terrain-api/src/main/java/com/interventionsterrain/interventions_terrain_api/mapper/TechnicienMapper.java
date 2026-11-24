package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import org.springframework.stereotype.Component;

/** Conversion Technicien → TechnicienResponse ; l'agence est déduite de l'équipe. */
@Component
public class TechnicienMapper {

    private final EquipeSupport equipeSupport;

    public TechnicienMapper(EquipeSupport equipeSupport) {
        this.equipeSupport = equipeSupport;
    }

    public TechnicienResponse toResponse(Technicien technicien) {
        Equipe equipe = equipeSupport.findRequired(technicien.getIdEquipe());
        return new TechnicienResponse(
                technicien.getIdTechnicien(),
                technicien.getNom(),
                technicien.getPrenom(),
                technicien.getEmail(),
                technicien.getTelephone(),
                technicien.getMatricule(),
                technicien.getIdEquipe(),
                equipe.getIdAgence(),
                equipeSupport.nomAgence(equipe.getIdAgence()));
    }
}
