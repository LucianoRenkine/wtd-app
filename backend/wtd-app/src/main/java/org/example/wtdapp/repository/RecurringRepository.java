package org.example.wtdapp.repository;

import org.example.wtdapp.entity.Recurring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringRepository extends JpaRepository<Recurring, Long> {
    List<Recurring> findAllByOrderByNameAsc();
}
