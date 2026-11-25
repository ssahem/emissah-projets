package com.suiviscolaire.suivi_scolaire_api.mapper;

import com.suiviscolaire.suivi_scolaire_api.dto.response.EleveResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import org.springframework.stereotype.Component;

/**
 * Conversion Eleve ↔ EleveResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire une entité en DTO en résolvant l'inscription courante et la classe.
 * DÉPEND DE    : Eleve, EleveResponse, InscriptionRepository, ClasseRepository.
 * NE FAIT PAS  : de règle métier.
 */
@Component
public class EleveMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final InscriptionRepository inscriptionRepository;
    private final ClasseRepository classeRepository;

    /** Spring injecte les dépôts d'inscription et de classe. */
    public EleveMapper(InscriptionRepository inscriptionRepository, ClasseRepository classeRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.classeRepository = classeRepository;
    }


    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit un élève en DTO ; classe et nom de classe sont null si aucune
     * inscription.
     */

    public EleveResponse toResponse(Eleve eleve) {
        if (eleve.getIdEleve() == null) {
            return new EleveResponse(null, eleve.getNom(), eleve.getPrenom(),
                    eleve.getDateNaissance(), null, null);
        }
        Inscription inscription = inscriptionRepository
                .findByIdEleveAndDateFinIsNull(eleve.getIdEleve()).orElse(null);
        if (inscription == null) {
            return new EleveResponse(eleve.getIdEleve(), eleve.getNom(), eleve.getPrenom(),
                    eleve.getDateNaissance(), null, null);
        }
        Classe classe = classeRepository.findById(inscription.getIdClasse()).orElse(null);
        return new EleveResponse(eleve.getIdEleve(), eleve.getNom(), eleve.getPrenom(),
                eleve.getDateNaissance(), inscription.getIdClasse(),
                classe == null ? null : classe.getNom());
    }
}
