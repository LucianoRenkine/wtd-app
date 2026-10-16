package org.example.wtdapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Un cumpleaños se guarda como día + mes (+ año de nacimiento opcional), no
 * como una fecha. La fecha de cada año se calcula al mostrarlo, así que no
 * hay nada que "renovar": el del año que viene ya existe.
 */
@Entity
@Table(name = "birthdays")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Birthday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "birth_month", nullable = false)
    private Integer month;

    @Column(name = "birth_day", nullable = false)
    private Integer day;

    /** Opcional: si está, se puede mostrar cuántos años cumple. */
    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
