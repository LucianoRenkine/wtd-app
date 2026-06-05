package org.example.wtdapp.repository;

import org.example.wtdapp.entity.Task;
import org.example.wtdapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE " +
           "(t.createdBy = :user OR :user MEMBER OF t.sharedWith) AND " +
           "t.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.startDate ASC, t.startTime ASC")
    List<Task> findTasksByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM Task t WHERE " +
           "(t.createdBy = :user OR :user MEMBER OF t.sharedWith) AND " +
           "t.startDate = :date " +
           "ORDER BY t.startTime ASC")
    List<Task> findTasksByUserAndDate(
        @Param("user") User user,
        @Param("date") LocalDate date
    );

    List<Task> findByCreatedBy(User user);

    @Query("SELECT t FROM Task t WHERE " +
           "(t.createdBy = :user OR :user MEMBER OF t.sharedWith) " +
           "ORDER BY t.startDate ASC")
    List<Task> findAllTasksForUser(@Param("user") User user);
}
