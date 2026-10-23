/* ── Service worker: recibe las notificaciones push y las muestra. ──
   No cachea nada (la app siempre trae lo último), solo existe para el push. */
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', e => e.waitUntil(self.clients.claim()));

self.addEventListener('push', event => {
    let data = {};
    try { data = event.data ? event.data.json() : {}; } catch (e) { data = { body: event.data && event.data.text() }; }
    const title = data.title || 'WTD';
    event.waitUntil(self.registration.showNotification(title, {
        body: data.body || '',
        icon: 'icons/icon-192.png',
        badge: 'icons/icon-192.png',
        tag: data.tag || undefined,   // mismo tag = reemplaza en vez de apilar
        data: { url: data.url || '/index.html' }
    }));
});

self.addEventListener('notificationclick', event => {
    event.notification.close();
    const url = new URL((event.notification.data && event.notification.data.url) || '/index.html', self.location.origin).href;
    event.waitUntil(self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then(list => {
        for (const c of list) {
            if ('focus' in c) { c.navigate(url).catch(() => {}); return c.focus(); }
        }
        return self.clients.openWindow(url);
    }));
});
