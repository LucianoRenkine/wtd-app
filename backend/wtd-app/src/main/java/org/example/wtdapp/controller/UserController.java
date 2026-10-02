package org.example.wtdapp.controller;

import org.example.wtdapp.entity.User;
import org.example.wtdapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public List<User> obtenerTodos() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User obtenerPorId(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    public User crearUsuario(@RequestBody User user) {
        return userRepository.save(user);
    }

    /* Login básico: compara la contraseña recibida contra el hash
       guardado. No arma sesión ni token — el frontend, si da ok, guarda
       el usuario en localStorage como ya hacía antes. */
    @PostMapping("/{id}/login")
    public ResponseEntity<?> login(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElse(null);
        String password = body.get("password");

        if (user == null || password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Contraseña incorrecta"));
        }
        return ResponseEntity.ok(user);
    }
}
