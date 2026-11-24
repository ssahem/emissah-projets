package com.flottevehicules.flotte_vehicules_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI de la flotte de véhicules.
 *
 * COUCHE       : Configuration
 * RÔLE         : décrire l'API dans Swagger UI, sans schéma de sécurité (aucune
 *                authentification).
 * NE FAIT PAS  : de logique métier.
 */
@Configuration
public class OpenApiConfig {

    /** Construit la documentation. */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("API de la flotte de véhicules")
                .version("v1")
                .description("Gestion des sites, véhicules, salariés, affectations et entretiens — API publique sans authentification"));
    }
}
