package org.example.wtdapp.repository;

import org.example.wtdapp.entity.Recurring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringRepository extends JpaRepository<Recurring, Long> {
    /** Las propias, las compartidas y las anteriores al dueño (ownerId null). */
    @Query("select r from Recurring r where r.ownerId is null or r.ownerId = :userId or r.shared = true order by r.name")
    List<Recurring> findVisibleTo(@Param("userId") Long userId);

    /** Sin saber quién pregunta: solo lo que ve cualquiera. */
    @Query("select r from Recurring r where r.ownerId is null or r.shared = true order by r.name")
    List<Recurring> findVisibleToAnyone();
}
