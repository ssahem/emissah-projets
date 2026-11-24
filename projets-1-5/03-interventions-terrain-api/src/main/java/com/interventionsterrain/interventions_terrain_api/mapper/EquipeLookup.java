package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import org.springframework.stereotype.Component;

/** Implémentation en lecture seule de {@link EquipeSupport}, sans dépendance vers un service. */
@Component
public class EquipeLookup implements EquipeSupport {

    private final EquipeRepository equipeRepository;
    private final AgenceRepository agenceRepository;

    public EquipeLookup(EquipeRepository equipeRepository, AgenceRepository agenceRepository) {
        this.equipeRepository = equipeRepository;
        this.agenceRepository = agenceRepository;
    }

    @Override
    public Equipe findRequired(Integer idEquipe) {
        return equipeRepository.findById(idEquipe)
                .orElseThrow(() -> new RessourceIntrouvableException("Équipe", idEquipe));
    }

    @Override
    public String nomAgence(Integer idAgence) {
        Agence agence = agenceRepository.findById(idAgence)
                .orElseThrow(() -> new RessourceIntrouvableException("Agence", idAgence));
        return agence.getNom();
    }
}
