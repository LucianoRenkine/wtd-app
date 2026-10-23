package org.example.wtdapp.controller;

import org.example.wtdapp.service.ChangelogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.List;

/** Novedades de la app (para la página "Novedades"). */
@RestController
@RequestMapping("/changelog")
@CrossOrigin(origins = "*")
public class ChangelogController {

    @Autowired
    private ChangelogService changelog;

    @GetMapping
    public List<JsonNode> all() {
        return changelog.all();
    }
}
