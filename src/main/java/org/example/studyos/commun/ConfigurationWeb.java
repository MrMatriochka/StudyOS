package org.example.studyos.commun;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS ouvert uniquement pour le front Vite en local (application mono-utilisateur).
 * Pas de Spring Security dans les lots 0 et 1 : cette regle suffit.
 */
@Configuration
public class ConfigurationWeb implements WebMvcConfigurer {

    private final String origineAutorisee;

    public ConfigurationWeb(@Value("${studyos.cors.allowed-origin}") String origineAutorisee) {
        this.origineAutorisee = origineAutorisee;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origineAutorisee)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
