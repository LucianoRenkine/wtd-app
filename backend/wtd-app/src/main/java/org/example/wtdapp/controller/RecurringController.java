package org.example.wtdapp.controller;

import org.example.wtdapp.entity.Recurring;
import org.example.wtdapp.repository.RecurringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Suscripciones y tareas periódicas. Cada una es privada de su dueño salvo que se marque como compartida.
 */
@RestController
@RequestMapping("/recurrings")
@CrossOrigin(origins = "*")
public class RecurringController {

    @Autowired
    private RecurringRepository recurringRepository;

    @GetMapping
    public List<Recurring> getAll(@RequestParam(required = false) Long userId) {
        return userId == null ? recurringRepository.findVisibleToAnyone() : recurringRepository.findVisibleTo(userId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Recurring r) {
        String error = validate(r);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));
        Recurring created = clean(r, new Recurring());
        created.setOwnerId(r.getOwnerId());
        created.setShared(Boolean.TRUE.equals(r.getShared()));
        return ResponseEntity.ok(recurringRepository.save(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Recurring details) {
        Optional<Recurring> existing = recurringRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        String error = validate(details);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));
        Recurring r = clean(details, existing.get());
        // details.ownerId es quien edita. Solo el dueño (o cualquiera, si no tenía dueño) cambia si es compartida.
        boolean isOwner = r.getOwnerId() == null || r.getOwnerId().equals(details.getOwnerId());
        if (isOwner) {
            r.setShared(Boolean.TRUE.equals(details.getShared()));
            if (r.getOwnerId() == null) r.setOwnerId(details.getOwnerId());
        }
        return ResponseEntity.ok(recurringRepository.save(r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recurringRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String validate(Recurring r) {
        if (r.getName() == null || r.getName().isBlank()) return "Falta el nombre";
        if (r.getKind() == null) return "Falta el tipo";
        if (r.getFrequency() == null) return "Falta la frecuencia";
        if (r.getStartDate() == null) return "Falta la fecha";
        if (r.getFrequency() == Recurring.Frequency.MONTHLY_WEEKDAY) {
            if (r.getWeekOfMonth() == null || r.getWeekOfMonth() < 1 || r.getWeekOfMonth() > 5) return "Elegí qué semana del mes";
            if (r.getWeekday() == null || r.getWeekday() < 0 || r.getWeekday() > 6) return "Elegí el día de la semana";
        }
        if (r.getEndDate() != null && r.getEndDate().isBefore(r.getStartDate())) return "El fin no puede ser anterior al inicio";
        if (r.getKind() == Recurring.Kind.SUBSCRIPTION && r.getAmount() != null && r.getAmount() < 0) return "Costo inválido";
        return null;
    }

    /** Copia solo los campos editables; el costo solo aplica a suscripciones. */
    private Recurring clean(Recurring from, Recurring to) {
        boolean sub = from.getKind() == Recurring.Kind.SUBSCRIPTION;
        to.setKind(from.getKind());
        to.setName(from.getName().trim());
        to.setAmount(sub ? from.getAmount() : null);
        to.setCurrency(sub ? (from.getCurrency() == null || from.getCurrency().isBlank() ? "ARS" : from.getCurrency().trim()) : null);
        to.setFrequency(from.getFrequency());
        to.setStartDate(from.getStartDate());
        to.setEndDate(from.getEndDate());
        boolean nth = from.getFrequency() == Recurring.Frequency.MONTHLY_WEEKDAY;
        to.setWeekOfMonth(nth ? from.getWeekOfMonth() : null);
        to.setWeekday(nth ? from.getWeekday() : null);
        to.setSkippedDates(from.getSkippedDates() == null || from.getSkippedDates().isBlank() ? null : from.getSkippedDates().trim());
        to.setNote(from.getNote() == null || from.getNote().isBlank() ? null : from.getNote().trim());
        return to;
    }
}
