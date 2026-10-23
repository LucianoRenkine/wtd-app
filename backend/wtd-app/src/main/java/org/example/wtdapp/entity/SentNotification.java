package org.example.wtdapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Registro de avisos ya enviados. La clave describe qué se avisó y para qué
 * día ("birthday:3:2026-10-24"), así el job puede correr cada minuto sin
 * repetir nada, aunque el servidor se reinicie.
 */
@Entity
@Table(name = "sent_notifications")
@Data
@NoArgsConstructor
public class SentNotification {

    @Id
    @Column(name = "notif_key", length = 200)
    private String key;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    public SentNotification(String key) {
        this.key = key;
        this.sentAt = LocalDateTime.now();
    }
}
