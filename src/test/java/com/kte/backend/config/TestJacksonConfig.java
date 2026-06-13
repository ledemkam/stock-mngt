package com.kte.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de test fournissant un ObjectMapper utilisable par les @WebMvcTest.
 */
@Configuration
public class TestJacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Enregistre automatiquement les modules (p.ex. JavaTimeModule)
        mapper.findAndRegisterModules();
        return mapper;
    }
}
