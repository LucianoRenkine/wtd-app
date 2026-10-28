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
 * Es privada de su dueño (ownerId) hasta que se la marca como compartida.
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

    /** Perfil que la creó. Null = viene de antes de que existiera el dueño: la ven todos hasta que alguien la edite. */
    private Long ownerId;

    /** Si es true la ve también el otro perfil. Por defecto es privada. */
    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private Boolean shared = false;

    /** Solo MONTHLY_WEEKDAY: qué semana del mes (1 a 4, o 5 = la última). */
    private Integer weekOfMonth;

    /** Solo MONTHLY_WEEKDAY: día de la semana, 0 = domingo … 6 = sábado (igual que getDay() de JavaScript). */
    private Integer weekday;

    /** Días puntuales cancelados ("YYYY-MM-DD" separados por coma): la serie sigue, solo esa vez no cae. */
    @Column(columnDefinition = "TEXT")
    private String skippedDates;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (shared == null) shared = false;
    }

    public enum Kind { SUBSCRIPTION, TASK }

    /** MONTHLY_WEEKDAY = "el primer sábado de cada mes", "el último viernes"… */
    public enum Frequency { WEEKLY, MONTHLY, MONTHLY_WEEKDAY, YEARLY }
}
