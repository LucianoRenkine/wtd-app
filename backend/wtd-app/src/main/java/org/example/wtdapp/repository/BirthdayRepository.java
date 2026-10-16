package org.example.wtdapp.repository;

import org.example.wtdapp.entity.Birthday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BirthdayRepository extends JpaRepository<Birthday, Long> {
    List<Birthday> findAllByOrderByMonthAscDayAscNameAsc();
}
