package com.suiviscolaire.suivi_scolaire_api.mapper;

import com.suiviscolaire.suivi_scolaire_api.dto.response.NoteResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.entity.Note;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.entity.TypeEvaluation;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TypeEvaluationRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Conversion Note ↔ NoteResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : résout les libellés et la date du trimestre pour enrichir une note.
 * DÉPEND DE    : Note, les dépôts de référence et les DTO.
 * NE FAIT PAS  : de calcul de moyenne ni de règle métier.
 */
@Component
public class NoteMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final InscriptionRepository inscriptionRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final TypeEvaluationRepository typeRepository;
    private final ClasseRepository classeRepository;
    private final TrimestreRepository trimestreRepository;

    /** Spring injecte les dépôts de référence. */
    public NoteMapper(InscriptionRepository inscriptionRepository,
                      EnseignementRepository enseignementRepository,
                      MatiereRepository matiereRepository,
                      TypeEvaluationRepository typeRepository,
                      ClasseRepository classeRepository,
                      TrimestreRepository trimestreRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.typeRepository = typeRepository;
        this.classeRepository = classeRepository;
        this.trimestreRepository = trimestreRepository;
    }

    /** Convertit une note en DTO ; la date affichée est le début du trimestre (A-03). */

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Exécute toResponse().
     */

    public NoteResponse toResponse(Note note) {
        Inscription inscription = inscriptionRepository.findByNumeroInscription(note.getNumeroInscription())
                .orElse(null);
        Integer idEleve = inscription == null ? null : inscription.getIdEleve();
        String nomEleve = null;
        if (idEleve != null) {
            Eleve eleve = inscription.getEleve();
            nomEleve = eleve == null ? null : eleve.getNom();
        }
        Enseignement enseignement = enseignementRepository.findById(note.getIdEnseignement()).orElse(null);
        Integer idClasse = enseignement == null ? null : enseignement.getIdClasse();
        String nomClasse = null;
        if (idClasse != null) {
            Classe classe = classeRepository.findById(idClasse).orElse(null);
            nomClasse = classe == null ? null : classe.getNom();
        }
        Matiere matiere = enseignement == null ? null
                : matiereRepository.findById(enseignement.getIdMatiere()).orElse(null);
        TypeEvaluation type = typeRepository.findById(note.getIdTypeEval()).orElse(null);
        LocalDate date = trimestreRepository.findByNumero(note.getNumeroTrimestre())
                .map(Trimestre::getDateDebut).orElse(null);
        return new NoteResponse(note.getIdNote(), idEleve, nomEleve,
                matiere == null ? null : matiere.getIdMatiere(),
                matiere == null ? null : matiere.getLibelle(),
                idClasse, nomClasse, note.getValeur(), note.getNumeroTrimestre(),
                note.getIdTypeEval(), type == null ? null : type.getLibelle(),
                type == null ? null : type.getPoids(), date);
    }
}
