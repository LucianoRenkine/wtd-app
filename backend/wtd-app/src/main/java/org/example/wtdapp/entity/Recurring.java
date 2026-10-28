package org.example.wtdapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Algo que se repite solo: una suscripción (con costo) o una tarea periódica.
 * No se guardan las ocurrencias: se guarda la fecha de inicio + la frecuencia
 * y el front calcula en qué días cae. Sin endDate dura por tiempo indeterminado.
 */
@Entity
@Table(name = "recurrings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recurring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Kind kind;

    @Column(nullable = false)
    private String name;

    /** Solo suscripciones. */
    private Double amount;

    @Column(length = 8)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;

    /** Primera ocurrencia: de acá salen el día de la semana, del mes o del año. */
    @Column(nullable = false)
    private LocalDate startDate;

    /** Opcional. Vacío = por tiempo indeterminado. */
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    /** Días puntuales cancelados ("YYYY-MM-DD" separados por coma): la serie sigue, solo esa vez no cae. */
    @Column(columnDefinition = "TEXT")
    private String skippedDates;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Kind { SUBSCRIPTION, TASK }

    public enum Frequency { WEEKLY, MONTHLY, YEARLY }
}
