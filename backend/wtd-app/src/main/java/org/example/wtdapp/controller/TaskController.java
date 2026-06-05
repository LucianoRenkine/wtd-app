package org.example.wtdapp.controller;

import org.example.wtdapp.entity.Task;
import org.example.wtdapp.entity.User;
import org.example.wtdapp.entity.Category;
import org.example.wtdapp.service.TaskService;
import org.example.wtdapp.repository.UserRepository;
import org.example.wtdapp.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Task> getAllTasks(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (userId == null) {
            return List.of();
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return List.of();
        }

        if (date != null) {
            return taskService.getTasksByUserAndDate(user.get(), date);
        }

        if (from != null && to != null) {
            return taskService.getTasksByUserAndDateRange(user.get(), from, to);
        }

        return taskService.getTasksByUser(user.get());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        // Load category if provided
        if (task.getCategory() != null && task.getCategory().getId() != null) {
            Optional<Category> category = categoryRepository.findById(task.getCategory().getId());
            if (category.isEmpty()) {
                return ResponseEntity.badRequest().body("Categoría no encontrada");
            }
            task.setCategory(category.get());
        }

        // Load user if provided
        if (task.getCreatedBy() != null && task.getCreatedBy().getId() != null) {
            Optional<User> user = userRepository.findById(task.getCreatedBy().getId());
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }
            task.setCreatedBy(user.get());
        }

        Task created = taskService.createTask(task);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        Task updated = taskService.updateTask(id, taskDetails);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/share/{userId}")
    public ResponseEntity<Task> shareTask(@PathVariable Long taskId, @PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task shared = taskService.shareTaskWithUser(taskId, user.get());
        if (shared != null) {
            return ResponseEntity.ok(shared);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{taskId}/share/{userId}")
    public ResponseEntity<Task> unshareTask(@PathVariable Long taskId, @PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task unshared = taskService.removeTaskShare(taskId, user.get());
        if (unshared != null) {
            return ResponseEntity.ok(unshared);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}
