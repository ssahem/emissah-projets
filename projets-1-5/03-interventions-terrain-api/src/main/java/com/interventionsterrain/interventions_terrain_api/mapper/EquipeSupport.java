package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.entity.Equipe;

/**
 * Fournit au mapper les données d'équipe et d'agence nécessaires à TechnicienResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire l’equipesupport et son DTO, sans logique métier.
 * DÉPEND DE    : l’entité et le DTO uniquement.
 * UTILISÉ PAR  : les services.
 * NE FAIT PAS  : d’accès base, de HTTP ni de règle métier.
 */
public interface EquipeSupport {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    Equipe findRequired(Integer idEquipe);

    String nomAgence(Integer idAgence);
}
