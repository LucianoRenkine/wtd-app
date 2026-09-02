package org.example.wtdapp.scheduler;

import org.example.wtdapp.entity.Task;
import org.example.wtdapp.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Las Tareas las completa la persona. Los Eventos (una clase, un discurso,
 * algo que depende del reloj y no de vos) se completan solos cuando termina
 * su horario. Este job corre cada 5 minutos y revisa los Eventos pendientes.
 */
@Component
public class EventAutoCompleteJob {

    private static final Logger log = LoggerFactory.getLogger(EventAutoCompleteJob.class);

    @Autowired
    private TaskRepository taskRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void autoCompletePastEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> pendingEvents = taskRepository.findByItemTypeAndCompletedFalse(Task.ItemType.EVENT);

        int completedCount = 0;
        for (Task event : pendingEvents) {
            LocalDateTime effectiveEnd = effectiveEnd(event);
            if (effectiveEnd != null && !effectiveEnd.isAfter(now)) {
                event.setCompleted(true);
                taskRepository.save(event);
                completedCount++;
            }
        }

        if (completedCount > 0) {
            log.info("EventAutoCompleteJob: {} evento(s) marcado(s) como completado(s)", completedCount);
        }
    }

    /** Momento en que un evento se considera terminado. */
    private LocalDateTime effectiveEnd(Task t) {
        LocalDate endDate = t.getEndDate() != null ? t.getEndDate() : t.getStartDate();
        if (endDate == null) return null;

        if (Boolean.TRUE.equals(t.getAllDay())) {
            return LocalDateTime.of(endDate, LocalTime.MAX);
        }

        LocalTime endTime = t.getEndTime() != null ? t.getEndTime() : t.getStartTime();
        if (endTime == null) {
            // Sin ningún horario: lo tratamos como si durara todo el día.
            return LocalDateTime.of(endDate, LocalTime.MAX);
        }
        return LocalDateTime.of(endDate, endTime);
    }
}
