package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.entity.Equipe;

/**
 * Fournit au mapper les données d'équipe et d'agence nécessaires à TechnicienResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : convertir EquipeSupport entre couches, sans accès base ni logique métier.
 * DÉPEND DE    : les entités et les DTO EquipeSupport.
 * UTILISÉ PAR  : les services.
 * NE FAIT PAS  : de logique métier ni de persistance.
 */
public interface EquipeSupport {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    Equipe findRequired(Integer idEquipe);

    String nomAgence(Integer idAgence);
}
