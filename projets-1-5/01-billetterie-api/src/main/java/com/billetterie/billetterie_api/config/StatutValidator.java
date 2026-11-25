package com.billetterie.billetterie_api.config;

import com.billetterie.billetterie_api.repository.StatutRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Vérification de configuration au démarrage.
 *
 * COUCHE       : Configuration
 * RÔLE         : refuser de démarrer si un statut métier indispensable manque.
 * NE FAIT PAS  : de migration ni d'insertion de données en production.
 */
@Component
public class StatutValidator implements ApplicationRunner {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    private static final List<String> REQUIRED = List.of("PROVISOIRE", "PAYEE", "ANNULEE");

    private final StatutRepository statutRepository;

    public StatutValidator(StatutRepository statutRepository) {
        this.statutRepository = statutRepository;
    }

    // ── run() ────────────────────────────────────────────────────────────────

    /** Échoue au démarrage si la table statut n'est pas exploitable. */
    @Override
    public void run(ApplicationArguments args) {
        List<String> missing = REQUIRED.stream()
                .filter(libelle -> statutRepository.findByLibelleIgnoreCase(libelle).isEmpty())
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Statuts manquants en base : " + missing);
        }
    }
}
