package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Résolution commune des trimestres par numéro (arbitrage A-03).
 *
 * COUCHE       : Service (outil partagé)
 * RÔLE         : valider le numéro 1 à 3, vérifier l'existence et l'état de clôture.
 * UTILISÉ PAR  : NoteService, ReglesMetierService, ConsultationService, RestitutionService.
 * NE FAIT PAS  : de HTTP ni d'écriture en base.
 */
@Component
public class TrimestreLookup {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final TrimestreRepository repository;

    /** Spring injecte le dépôt de trimestres. */
    public TrimestreLookup(TrimestreRepository repository) {
        this.repository = repository;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════


    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Retourne le trimestre de numéro donné, ou 400/404 si le numéro est
     * invalide/inconnu.
     */

    public Trimestre required(Integer numero) {
        if (numero == null || numero < 1 || numero > 3) {
            throw new DonneeInvalideException("trimestre-numero-invalide",
                    "Le numéro de trimestre doit être compris entre 1 et 3",
                    Map.of("numero", numero));
        }
        return repository.findByNumero(numero)
                .orElseThrow(() -> new RessourceIntrouvableException("Trimestre", numero));
    }


    // ── exigible() ───────────────────────────────────────────────────────────

    /**
     * Retourne le trimestre en interdisant toute écriture de notes après
     * clôture.
     */

    public Trimestre exigible(Integer numero, String operation) {
        Trimestre trimestre = required(numero);
        if (trimestre.isEstCloture()) {
            throw new com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException(
                    "trimestre-cloture",
                    "Le trimestre " + numero + " est clôturé : " + operation + " est interdite",
                    Map.of("trimestre", numero));
        }
        return trimestre;
    }
}
