package org.example.wtdapp.repository;

import org.example.wtdapp.entity.SentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SentNotificationRepository extends JpaRepository<SentNotification, String> {
    void deleteBySentAtBefore(LocalDateTime cutoff);
}
