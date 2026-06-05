package org.example.wtdapp.controller;

import org.example.wtdapp.entity.User;
import org.example.wtdapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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
}