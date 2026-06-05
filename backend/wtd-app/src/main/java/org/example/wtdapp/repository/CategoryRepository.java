package org.example.wtdapp.repository;

import org.example.wtdapp.entity.Category;
import org.example.wtdapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByOwner(User owner);
}
