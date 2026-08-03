package com.orientation.orientationapp;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class OrientationAppApplication {

    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );
            log.info("Loaded {} variables from .env", dotenv.entries().size());
        } catch (Exception e) {
            log.info("No .env file found — using system environment variables");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(OrientationAppApplication.class, args);
    }
}
