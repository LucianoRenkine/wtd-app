package org.example.wtdapp.controller;

import org.example.wtdapp.entity.Birthday;
import org.example.wtdapp.repository.BirthdayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cumpleaños compartidos: una sola lista para todos los perfiles.
 */
@RestController
@RequestMapping("/birthdays")
@CrossOrigin(origins = "*")
public class BirthdayController {

    @Autowired
    private BirthdayRepository birthdayRepository;

    @GetMapping
    public List<Birthday> getAll() {
        return birthdayRepository.findAllByOrderByMonthAscDayAscNameAsc();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Birthday b) {
        String error = validate(b);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));
        b.setId(null);
        return ResponseEntity.ok(birthdayRepository.save(clean(b)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Birthday details) {
        Optional<Birthday> existing = birthdayRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        String error = validate(details);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));

        Birthday b = existing.get();
        Birthday c = clean(details);
        b.setName(c.getName());
        b.setMonth(c.getMonth());
        b.setDay(c.getDay());
        b.setBirthYear(c.getBirthYear());
        b.setNote(c.getNote());
        return ResponseEntity.ok(birthdayRepository.save(b));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        birthdayRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Nombre obligatorio, mes 1-12 y un día que exista en ese mes (29/2 vale). */
    private String validate(Birthday b) {
        if (b.getName() == null || b.getName().isBlank()) return "Falta el nombre";
        if (b.getMonth() == null || b.getMonth() < 1 || b.getMonth() > 12) return "Mes inválido";
        if (b.getDay() == null || b.getDay() < 1 || b.getDay() > Month.of(b.getMonth()).maxLength()) return "Día inválido";
        if (b.getBirthYear() != null && (b.getBirthYear() < 1900 || b.getBirthYear() > LocalDate.now().getYear())) return "Año inválido";
        return null;
    }

    private Birthday clean(Birthday b) {
        Birthday c = new Birthday();
        c.setName(b.getName().trim());
        c.setMonth(b.getMonth());
        c.setDay(b.getDay());
        c.setBirthYear(b.getBirthYear());
        c.setNote(b.getNote() == null || b.getNote().isBlank() ? null : b.getNote().trim());
        return c;
    }
}
