package org.example.wtdapp.service;

import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.example.wtdapp.entity.PushSubscription;
import org.example.wtdapp.repository.PushSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Envía notificaciones push a los dispositivos suscriptos. Si no hay claves
 * VAPID configuradas queda desactivado (no rompe nada, solo no avisa).
 */
@Service
public class WtdPushService {

    private static final Logger log = LoggerFactory.getLogger(WtdPushService.class);

    @Value("${vapid.public-key:}")
    private String publicKey;
    @Value("${vapid.private-key:}")
    private String privateKey;
    @Value("${vapid.subject:mailto:wtd-app@example.com}")
    private String subject;

    @Autowired
    private PushSubscriptionRepository subscriptions;

    private PushService client;

    @PostConstruct
    void init() {
        if (publicKey.isBlank() || privateKey.isBlank()) {
            log.warn("Push desactivado: faltan VAPID_PUBLIC_KEY / VAPID_PRIVATE_KEY");
            return;
        }
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            client = new PushService(publicKey, privateKey, subject);
        } catch (Exception e) {
            log.error("No se pudo iniciar el push: {}", e.getMessage());
        }
    }

    public boolean enabled() { return client != null; }

    public String publicKey() { return publicKey; }

    /** Manda el aviso a todos los dispositivos de esos usuarios. Devuelve cuántos recibieron. */
    public int sendToUsers(Collection<Long> userIds, String title, String body, String url, String tag) {
        int sent = 0;
        for (Long id : userIds) {
            sent += sendTo(subscriptions.findByUserId(id), title, body, url, tag);
        }
        return sent;
    }

    public int sendToAll(String title, String body, String url, String tag) {
        return sendTo(subscriptions.findAll(), title, body, url, tag);
    }

    private int sendTo(List<PushSubscription> subs, String title, String body, String url, String tag) {
        int ok = 0;
        for (PushSubscription s : subs) {
            if (sendOne(s, title, body, url, tag)) ok++;
        }
        return ok;
    }

    /** Manda a un solo dispositivo. true si el servicio de push lo aceptó. */
    public boolean sendOne(PushSubscription s, String title, String body, String url, String tag) {
        if (client == null) return false;
        String payload = json(Map.of("title", title, "body", body, "url", url == null ? "/index.html" : url, "tag", tag == null ? "wtd" : tag));
        try {
            HttpResponse r = client.send(new Notification(s.getEndpoint(), s.getP256dh(), s.getAuth(), payload));
            int code = r.getStatusLine().getStatusCode();
            if (code == 404 || code == 410) {
                // El dispositivo ya no existe (app desinstalada / permiso revocado).
                subscriptions.deleteByEndpoint(s.getEndpoint());
            } else if (code >= 200 && code < 300) {
                return true;
            } else {
                log.warn("Push rechazado ({}) para suscripción {}", code, s.getId());
            }
        } catch (Exception e) {
            log.warn("Fallo enviando push a {}: {}", s.getId(), e.getMessage());
        }
        return false;
    }

    private static String json(Map<String, String> m) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : m.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey()).append("\":\"").append(esc(e.getValue())).append('"');
        }
        return sb.append('}').toString();
    }

    private static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> { if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.toString();
    }
}
