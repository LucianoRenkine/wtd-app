package org.example.wtdapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private Boolean allDay = false;

    @Column(columnDefinition = "TEXT")
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 20, columnDefinition = "varchar(20) not null default 'TASK'")
    private ItemType itemType = ItemType.TASK;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private Boolean completed = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority = Priority.NONE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_shared_with",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> sharedWith = new HashSet<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    /**
     * Jackson arma el objeto con el constructor de todos los argumentos que
     * genera Lombok, así que cualquier campo ausente en el JSON llega en null
     * acá, salteándose por completo el valor por defecto declarado en el
     * campo. Sin esto, omitir "itemType" en un POST rompe el not-null de la
     * columna en vez de caer en TASK.
     */
    private void applyDefaults() {
        if (completed == null) completed = false;
        if (priority == null) priority = Priority.NONE;
        if (itemType == null) itemType = ItemType.TASK;
        if (allDay == null) allDay = false;
    }

    public enum Priority {
        NONE, LOW, MEDIUM, HIGH
    }

    public enum RecurrenceType {
        NONE, DAILY, WEEKLY, MONTHLY
    }

    public enum ItemType {
        TASK, EVENT
    }
}
