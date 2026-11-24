package com.suiviscolaire.suivi_scolaire_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI de l'API de suivi scolaire.
 *
 * COUCHE       : Configuration
 * RÔLE         : décrire l'API dans Swagger, sans authentification.
 * NE FAIT PAS  : de logique métier ni de sécurité.
 */
@Configuration
public class OpenApiConfig {

    /** Construit la documentation. */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("API du suivi scolaire")
                .version("v1")
                .description("Classes, élèves, matières, professeurs, notes, bulletins et alertes — API publique"));
    }
}
