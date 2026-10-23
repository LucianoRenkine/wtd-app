package org.example.wtdapp.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Las novedades viven en resources/changelog.json (la más nueva arriba). Publicar
 * una novedad = agregar una entrada ahí: aparece en la página "Novedades" y se
 * avisa por push una sola vez a cada dispositivo (ver NotificationJob).
 */
@Service
public class ChangelogService {

    private static final Logger log = LoggerFactory.getLogger(ChangelogService.class);

    private final JsonMapper json = JsonMapper.builder().build();
    private List<JsonNode> entries = List.of();

    @PostConstruct
    void load() {
        try (InputStream in = new ClassPathResource("changelog.json").getInputStream()) {
            JsonNode root = json.readTree(in);
            List<JsonNode> list = new ArrayList<>();
            root.forEach(list::add);
            entries = list;
        } catch (Exception e) {
            log.warn("No se pudo leer changelog.json: {}", e.getMessage());
        }
    }

    /** Todas las novedades, de la más nueva a la más vieja. */
    public List<JsonNode> all() { return entries; }
}
