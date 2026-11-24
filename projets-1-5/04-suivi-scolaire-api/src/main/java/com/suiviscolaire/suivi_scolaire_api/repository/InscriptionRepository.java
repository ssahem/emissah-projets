package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux inscriptions (F2, F6, F11, F16).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : retrouver l'inscription courante d'un élève et les inscriptions d'une classe.
 * UTILISÉ PAR  : EleveService, ReglesMetierService, ConsultationService,
 *                AlertesDifficulteService.
 * NE FAIT PAS  : de règle métier.
 */
public interface InscriptionRepository extends JpaRepository<Inscription, Integer> {

    /** Inscription courante d'un élève (date_fin null). */
    Optional<Inscription> findByIdEleveAndDateFinIsNull(Integer idEleve);

    /** Toutes les inscriptions d'un élève, y compris les anciennes affectations (F11). */
    List<Inscription> findAllByIdEleveOrderByDateInscriptionAsc(Integer idEleve);

    /** Inscriptions courantes d'une classe (F6). */
    List<Inscription> findAllByIdClasseAndDateFinIsNull(Integer idClasse);

    /** Nombre d'inscriptions courantes d'une classe (effectif, F1/F6). */
    long countByIdClasseAndDateFinIsNull(Integer idClasse);

    /** Résout une note orpheline par son numéro d'inscription (A-01). */
    Optional<Inscription> findByNumeroInscription(String numeroInscription);

    /** Vérifie la présence de notes pour un numéro d'inscription sans FK (A-01). */
    @Query("select case when count(n) > 0 then true else false end from Note n where n.numeroInscription = :numero")
    boolean hasNotes(@Param("numero") String numero);
}
