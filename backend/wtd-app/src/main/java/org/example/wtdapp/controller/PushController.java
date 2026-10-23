package org.example.wtdapp.controller;

import org.example.wtdapp.entity.PushSubscription;
import org.example.wtdapp.repository.PushSubscriptionRepository;
import org.example.wtdapp.repository.UserRepository;
import org.example.wtdapp.service.WtdPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Alta/baja de dispositivos para notificaciones push y clave pública VAPID.
 */
@RestController
@RequestMapping("/push")
@CrossOrigin(origins = "*")
public class PushController {

    @Autowired
    private WtdPushService push;
    @Autowired
    private PushSubscriptionRepository subscriptions;
    @Autowired
    private UserRepository users;

    /** Clave pública que el navegador necesita para suscribirse. */
    @GetMapping("/key")
    public ResponseEntity<?> key() {
        if (!push.enabled()) return ResponseEntity.status(503).body(Map.of("error", "Las notificaciones no están configuradas en el servidor."));
        return ResponseEntity.ok(Map.of("publicKey", push.publicKey()));
    }

    /** Cuántos dispositivos tiene registrados esa persona (para mostrar el estado). */
    @GetMapping("/status")
    public Map<String, Object> status(@RequestParam Long userId, @RequestParam(required = false) String endpoint) {
        List<PushSubscription> mine = subscriptions.findByUserId(userId);
        boolean thisDevice = endpoint != null && mine.stream().anyMatch(s -> s.getEndpoint().equals(endpoint));
        return Map.of("enabled", push.enabled(), "devices", mine.size(), "thisDevice", thisDevice);
    }

    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> body) {
        Object uid = body.get("userId");
        Object sub = body.get("subscription");
        if (!(uid instanceof Number) || !(sub instanceof Map<?, ?> m)) return ResponseEntity.badRequest().body(Map.of("error", "Datos incompletos"));
        Long userId = ((Number) uid).longValue();
        if (users.findById(userId).isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));

        Object endpoint = m.get("endpoint");
        Object keys = m.get("keys");
        if (!(endpoint instanceof String ep) || !(keys instanceof Map<?, ?> k)
                || !(k.get("p256dh") instanceof String p256dh) || !(k.get("auth") instanceof String auth)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Suscripción inválida"));
        }
        // Un mismo dispositivo puede cambiar de perfil: se actualiza en vez de duplicar.
        PushSubscription s = subscriptions.findByEndpoint(ep).orElseGet(PushSubscription::new);
        s.setUserId(userId);
        s.setEndpoint(ep);
        s.setP256dh(p256dh);
        s.setAuth(auth);
        subscriptions.save(s);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/unsubscribe")
    @Transactional
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        if (endpoint != null) subscriptions.deleteByEndpoint(endpoint);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Manda una notificación de prueba a los dispositivos de esa persona. */
    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody Map<String, Object> body) {
        if (!(body.get("userId") instanceof Number n)) return ResponseEntity.badRequest().body(Map.of("error", "Falta userId"));
        int sent = push.sendToUsers(List.of(n.longValue()), "WTD", "Las notificaciones funcionan 🎉", "/index.html", "test");
        return ResponseEntity.ok(Map.of("sent", sent));
    }
}
