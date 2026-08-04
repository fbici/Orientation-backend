package com.orientation.orientationapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Convertit DATABASE_URL de Railway (postgresql://...) en JDBC URL (jdbc://postgresql://...).
 * Actif uniquement en profil "prod".
 */
@Configuration
@Profile("prod")
public class RailwayDatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        String jdbcUrl = databaseUrl;

        // Convertir postgresql:// en jdbc://postgresql://
        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            if (jdbcUrl.startsWith("postgresql://")) {
                jdbcUrl = "jdbc:" + jdbcUrl;
            } else if (jdbcUrl.startsWith("postgres://")) {
                jdbcUrl = "jdbc:" + jdbcUrl.replace("postgres://", "postgresql://");
            }
        }

        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .build();
    }
}
