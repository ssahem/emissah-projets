package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.entity.Equipe;

/** Fournit au mapper les données d'équipe et d'agence nécessaires à TechnicienResponse. */
public interface EquipeSupport {

    Equipe findRequired(Integer idEquipe);

    String nomAgence(Integer idAgence);
}
