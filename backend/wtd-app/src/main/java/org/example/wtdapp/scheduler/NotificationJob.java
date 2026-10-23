package org.example.wtdapp.scheduler;

import org.example.wtdapp.entity.Birthday;
import org.example.wtdapp.entity.PushSubscription;
import org.example.wtdapp.entity.SentNotification;
import org.example.wtdapp.entity.Task;
import org.example.wtdapp.entity.User;
import org.example.wtdapp.entity.WatchlistItem;
import org.example.wtdapp.repository.BirthdayRepository;
import org.example.wtdapp.repository.PushSubscriptionRepository;
import org.example.wtdapp.repository.SentNotificationRepository;
import org.example.wtdapp.repository.TaskRepository;
import org.example.wtdapp.repository.WatchlistItemRepository;
import org.example.wtdapp.service.ChangelogService;
import org.example.wtdapp.service.WtdPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Decide qué avisar y a quién. Corre cada minuto; cada aviso lleva una clave
 * en `sent_notifications`, así nunca se manda dos veces (ni siquiera si el
 * servidor se reinicia a mitad del día).
 *
 *  - Recordatorios: tareas/eventos con la campanita activa.
 *    Con hora: 15 min antes. Sin hora (todo el día): a las 9:00.
 *  - Cumpleaños: a las 9:00 del día. Va a todos los perfiles (lista compartida).
 *  - Estrenos: series/películas que salen hoy, a las 9:00. Todos los perfiles.
 *  - Novedades: cada entrada nueva de changelog.json se avisa una vez por dispositivo,
 *    apenas se publica (no depende de la hora ni de cuántos deploys haya).
 */
@Component
public class NotificationJob {

    private static final Logger log = LoggerFactory.getLogger(NotificationJob.class);

    private static final LocalTime MORNING = LocalTime.of(9, 0);
    private static final LocalTime LAST_MORNING_CALL = LocalTime.of(21, 0); // si el server estuvo caído, avisa tarde pero no de madrugada
    private static final Duration REMINDER_LEAD = Duration.ofMinutes(15);
    private static final Duration REMINDER_GRACE = Duration.ofMinutes(30);   // después del inicio ya no tiene sentido avisar

    private final JsonMapper json = JsonMapper.builder().build();

    @Value("${notifications.zone:America/Argentina/Buenos_Aires}")
    private String zoneId;

    @Autowired private WtdPushService push;
    @Autowired private TaskRepository tasks;
    @Autowired private BirthdayRepository birthdays;
    @Autowired private WatchlistItemRepository watchlist;
    @Autowired private SentNotificationRepository sent;
    @Autowired private PushSubscriptionRepository subscriptions;
    @Autowired private ChangelogService changelog;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void run() {
        if (!push.enabled()) return;
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));

        try { announceChangelog(now.toLocalDate()); } catch (Exception e) { log.warn("Novedades: {}", e.getMessage()); }
        try { reminders(now.toLocalDateTime()); } catch (Exception e) { log.warn("Recordatorios: {}", e.getMessage()); }
        LocalTime t = now.toLocalTime();
        if (!t.isBefore(MORNING) && t.isBefore(LAST_MORNING_CALL)) {
            try { birthdays(now.toLocalDate()); } catch (Exception e) { log.warn("Cumpleaños: {}", e.getMessage()); }
            try { premieres(now.toLocalDate()); } catch (Exception e) { log.warn("Estrenos: {}", e.getMessage()); }
        }
        if (t.getHour() == 3 && t.getMinute() == 0) sent.deleteBySentAtBefore(LocalDateTime.now().minusDays(60));
    }

    /** true si es la primera vez que se pide esa clave (y la deja anotada). */
    private boolean firstTime(String key) {
        if (sent.existsById(key)) return false;
        sent.save(new SentNotification(key));
        return true;
    }

    // ── Novedades de la app ────────────────────────────────────────────────
    /** Una novedad deja de avisarse a dispositivos nuevos pasados estos días (se puede leer igual en la página). */
    private static final int CHANGELOG_WINDOW_DAYS = 7;

    private void announceChangelog(LocalDate today) {
        List<PushSubscription> subs = null;
        // De la más vieja a la más nueva, así llegan en orden si se publican varias juntas.
        List<JsonNode> list = new java.util.ArrayList<>(changelog.all());
        java.util.Collections.reverse(list);
        for (JsonNode e : list) {
            String id = text(e, "id"), date = text(e, "date");
            if (id == null || date == null) continue;
            LocalDate published;
            try { published = LocalDate.parse(date); } catch (Exception ex) { continue; }
            if (today.isBefore(published) || today.isAfter(published.plusDays(CHANGELOG_WINDOW_DAYS))) continue;

            if (subs == null) subs = subscriptions.findAll();
            String title = "📣 WTD se actualizó", body = text(e, "summary") != null ? text(e, "summary") : text(e, "title");
            for (PushSubscription s : subs) {
                // Una clave por dispositivo: cada uno la recibe una sola vez, también el que active los avisos después.
                if (!firstTime("changelog:" + id + ":" + s.getId())) continue;
                push.sendOne(s, title, body, "/novedades.html", "changelog-" + id);
            }
        }
    }

    // ── Recordatorios ──────────────────────────────────────────────────────
    private void reminders(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        for (Task t : tasks.findByReminderTrueAndCompletedFalseAndStartDateBetween(today, today.plusDays(1))) {
            boolean timed = !Boolean.TRUE.equals(t.getAllDay()) && t.getStartTime() != null;
            LocalDateTime start = LocalDateTime.of(t.getStartDate(), timed ? t.getStartTime() : MORNING);
            LocalDateTime trigger = timed ? start.minus(REMINDER_LEAD) : start;
            LocalDateTime until = timed ? start.plus(REMINDER_GRACE) : LocalDateTime.of(t.getStartDate(), LAST_MORNING_CALL);
            if (now.isBefore(trigger) || !now.isBefore(until)) continue;

            String key = "task:" + t.getId() + ":" + t.getStartDate() + ":" + (timed ? t.getStartTime() : "allday");
            if (!firstTime(key)) continue;

            Set<Long> to = new HashSet<>();
            if (t.getCreatedBy() != null) to.add(t.getCreatedBy().getId());
            for (User u : t.getSharedWith()) to.add(u.getId());

            String body = timed
                ? "A las " + t.getStartTime().toString().substring(0, 5) + (now.isAfter(start) ? " (ya empezó)" : " · en " + Math.max(1, Duration.between(now, start).toMinutes()) + " min")
                : "Hoy, todo el día";
            push.sendToUsers(to, "🔔 " + t.getTitle(), body, "/calendar.html?edit=" + t.getId(), "task-" + t.getId());
        }
    }

    // ── Cumpleaños ─────────────────────────────────────────────────────────
    private void birthdays(LocalDate today) {
        for (Birthday b : birthdays.findAll()) {
            int day = b.getDay();
            if (b.getMonth() == 2 && day == 29 && !Year.isLeap(today.getYear())) day = 28;
            if (b.getMonth() != today.getMonthValue() || day != today.getDayOfMonth()) continue;
            if (!firstTime("birthday:" + b.getId() + ":" + today)) continue;

            String age = "";
            if (b.getBirthYear() != null && today.getYear() - b.getBirthYear() > 0) {
                age = " · cumple " + (today.getYear() - b.getBirthYear());
            }
            push.sendToAll("🎂 Hoy cumple " + b.getName(), "No te olvides de saludarle" + age, "/cumpleanos.html?q=" + enc(b.getName()), "birthday-" + b.getId());
        }
    }

    // ── Estrenos (series y películas de la Watchlist) ──────────────────────
    private void premieres(LocalDate today) {
        String d = today.toString();
        for (WatchlistItem w : watchlist.findAll()) {
            if (w.getData() == null) continue;
            JsonNode n;
            try { n = json.readTree(w.getData()); } catch (Exception e) { continue; }
            if ("DONE".equals(text(n, "list"))) continue;
            // El front corre la fecha del próximo estreno cuando alguien abre la app; por eso
            // también cuenta "salió hoy" (releasedOn), si no un cambio de día lo taparía.
            if (!d.equals(text(n, "releaseDate")) && !d.equals(text(n, "releasedOn"))) continue;
            if (!firstTime("premiere:" + w.getId() + ":" + d)) continue;

            String title = w.getTitle();
            if (w.getType() == WatchlistItem.MediaType.SERIES) {
                JsonNode s = n.path("schedule");
                String season = s.isObject() && s.hasNonNull("season") ? " (T" + s.path("season").asInt() + ")" : "";
                boolean full = s.isObject() && "FULL".equals(text(s, "mode"));
                String msg = full ? "Hoy sale la temporada completa" + season : "Hoy sale un nuevo episodio" + season;
                push.sendToAll("📺 " + title, msg, "/watchlist.html", "premiere-" + w.getId());
            } else {
                push.sendToAll("🎬 " + title, "Hoy se estrena", "/watchlist.html", "premiere-" + w.getId());
            }
        }
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asString();
    }

    private static String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
