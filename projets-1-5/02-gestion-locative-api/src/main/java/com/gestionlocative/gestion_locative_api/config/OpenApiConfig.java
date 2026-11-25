package com.gestionlocative.gestion_locative_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI de l'API de gestion locative.
 *
 * COUCHE       : Configuration
 * RÔLE         : décrire l'API dans Swagger.
 * DÉPEND DE    : aucune.
 * NE FAIT PAS  : de logique métier ni de sécurité (l'énoncé ne prévoit aucune authentification).
 */
@Configuration
public class OpenApiConfig {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    // ── openAPI() ────────────────────────────────────────────────────────────

    /** Construit la documentation. */
    @Bean
    public OpenAPI openAPI() {
        // ── Étape 1/1 : métadonnées ──────────────────────────────────────────
        Info info = new Info()
                .title("API de gestion locative")
                .version("v1")
                .description("Gestion des propriétaires, immeubles, appartements, locataires et baux");

        return new OpenAPI().info(info);
    }
}
