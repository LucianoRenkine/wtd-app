package org.example.wtdapp;

import org.example.wtdapp.entity.Category;
import org.example.wtdapp.repository.CategoryRepository;
import org.example.wtdapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Garantiza que exista la categoría "Final" (violeta) para cada usuario. */
@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategorySeeder(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.existsByName("Final")) return;
        userRepository.findAll().stream().findFirst().ifPresent(owner -> {
            Category c = new Category();
            c.setName("Final");
            c.setColor("#8e44ad");
            c.setOwner(owner);
            categoryRepository.save(c);
        });
    }
}
