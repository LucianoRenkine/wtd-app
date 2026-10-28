package org.example.wtdapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate crea un CHECK con los valores del enum la primera vez que arma la tabla
 * y `ddl-auto=update` nunca lo actualiza: al sumar MONTHLY_WEEKDAY, la base seguía
 * rechazando todo lo que no fuera WEEKLY / MONTHLY / YEARLY (error 500 al guardar).
 * Se quita el CHECK viejo; la validación de valores la hace el enum de Java.
 */
@Component
public class RecurringSchemaFix implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RecurringSchemaFix.class);

    private final JdbcTemplate jdbc;

    public RecurringSchemaFix(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            jdbc.execute("ALTER TABLE recurrings DROP CONSTRAINT IF EXISTS recurrings_frequency_check");
        } catch (Exception e) {
            log.warn("No se pudo quitar recurrings_frequency_check: {}", e.getMessage());
        }
    }
}
