package com.interventionsterrain.interventions_terrain_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI de l'API des interventions terrain.
 *
 * COUCHE       : Configuration
 * RÔLE         : décrire l'API dans Swagger.
 * NE FAIT PAS  : de logique métier ni de sécurité (l'énoncé ne prévoit aucun utilisateur).
 */
@Configuration
public class OpenApiConfig {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit la documentation. */

    // ── openAPI() ────────────────────────────────────────────────────────────

    /**
     * Exécute openAPI().
     */

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("API des interventions terrain")
                .version("v1")
                .description("Agences, techniciens, compétences, clients et interventions");

        return new OpenAPI().info(info);
    }
}
