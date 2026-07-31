package com.orientation.orientationapp.config.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orientationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orientation Platform API")
                        .description("""
                                API REST de la plateforme intelligente d'orientation universitaire.

                                ## Modules principaux
                                - **Auth** : Inscription, connexion, gestion des tokens
                                - **Universités** : CRUD universités, campus, facultés, programmes
                                - **Candidats** : Profils, relevés de notes, bulletins
                                - **Recommandations** : Moteur d'orientation intelligent
                                - **Documents** : Import, OCR, classification automatique
                                - **Knowledge** : Base de connaissances et Smart Query
                                - **Administration** : Utilisateurs, rôles, organisations, tenants
                                - **Backoffice** : Dashboard, monitoring, analytics, rapports

                                ## Authentification
                                Toutes les API (sauf /auth/login et /auth/register) nécessitent un token JWT.
                                Ajoutez le header : `Authorization: Bearer <token>`
                                """.stripIndent())
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Orientation Platform")
                                .url("https://github.com/fbici/Orientation-backend"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Développement local"),
                        new Server().url("https://api.orientation.com/api/v1").description("Production")
                ))
                .tags(List.of(
                        new Tag().name("Auth").description("Inscription, connexion, gestion des tokens"),
                        new Tag().name("Universities").description("CRUD universités, campus, facultés, programmes"),
                        new Tag().name("Candidates").description("Profils candidats, relevés de notes"),
                        new Tag().name("Recommendations").description("Moteur de recommandation intelligent"),
                        new Tag().name("Documents").description("Import, OCR, classification"),
                        new Tag().name("Knowledge").description("Base de connaissances, Smart Query"),
                        new Tag().name("Administration").description("Utilisateurs, rôles, organisations"),
                        new Tag().name("Backoffice").description("Dashboard, monitoring, analytics"),
                        new Tag().name("Locations").description("Pays, villes"),
                        new Tag().name("Intelligence").description("Pipeline documentaire, learning engine")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenu via /auth/login")));
    }
}
