package org.example.wtdapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private MediaType type = MediaType.MOVIE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private WatchStatus status = WatchStatus.SOON;

    private LocalDate releaseDate;

    private Integer season;

    private Integer year;

    /* El título completo tal como lo maneja el frontend (en qué lista está,
       progreso, calendario de estrenos, nota…) serializado como JSON. Las
       columnas de arriba quedan para consultas y compatibilidad. */
    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MediaType { MOVIE, SERIES }
    public enum WatchStatus { YES, NO, SOON }
}
