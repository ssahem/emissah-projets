package com.suiviscolaire.suivi_scolaire_api.mapper;

import com.suiviscolaire.suivi_scolaire_api.dto.response.ClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import org.springframework.stereotype.Component;

/**
 * Conversion Classe ↔ ClasseResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire une entité en DTO et ajouter l'effectif courant calculé.
 * DÉPEND DE    : Classe, ClasseResponse, InscriptionRepository.
 * NE FAIT PAS  : de règle métier ni de HTTP.
 */
@Component
public class ClasseMapper {

    private final InscriptionRepository inscriptionRepository;

    /** Spring injecte le dépôt d'inscriptions pour l'effectif courant. */
    public ClasseMapper(InscriptionRepository inscriptionRepository) {
        this.inscriptionRepository = inscriptionRepository;
    }

    /** Convertit une classe en DTO avec son effectif actuel (F1, F6). */
    public ClasseResponse toResponse(Classe classe) {
        long effectif = classe.getIdClasse() == null ? 0
                : inscriptionRepository.countByIdClasseAndDateFinIsNull(classe.getIdClasse());
        return new ClasseResponse(classe.getIdClasse(), classe.getNom(), classe.getIdAnnee(),
                classe.getIdNiveau(), classe.getEffectifMax(), effectif);
    }
}
