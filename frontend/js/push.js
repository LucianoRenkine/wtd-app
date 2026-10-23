/* ── Notificaciones push (iPhone) ──
   Registra el service worker y agrega "Notificaciones" al menú. En iOS el push
   solo existe cuando la app está agregada a la pantalla de inicio y se abre
   desde ahí; en una pestaña de Safari el navegador ni siquiera expone PushManager.
   Depende de api.js (API_BASE_URL, loadAuth, currentUserId). */
(function () {
    if (typeof API_BASE_URL === 'undefined') return;

    var isIOS = /iphone|ipad|ipod/i.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
    var standalone = window.navigator.standalone === true || (window.matchMedia && matchMedia('(display-mode: standalone)').matches);
    var supported = 'serviceWorker' in navigator && 'PushManager' in window && 'Notification' in window;

    var state = { kind: 'loading' };   // loading | unsupported | needsInstall | denied | off | on | unavailable
    var els = [];

    function userId() {
        if (!currentUserId && typeof loadAuth === 'function') loadAuth();
        return currentUserId;
    }

    function b64ToBytes(b64) {
        var pad = '='.repeat((4 - b64.length % 4) % 4);
        var raw = atob((b64 + pad).replace(/-/g, '+').replace(/_/g, '/'));
        return Uint8Array.from(raw, function (c) { return c.charCodeAt(0); });
    }

    function post(path, body) {
        return fetch(API_BASE_URL + path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    }

    async function registration() {
        return navigator.serviceWorker.register('sw.js').then(function () { return navigator.serviceWorker.ready; });
    }

    async function refresh() {
        if (!supported) {
            state = { kind: isIOS && !standalone ? 'needsInstall' : 'unsupported' };
        } else if (Notification.permission === 'denied') {
            state = { kind: 'denied' };
        } else {
            try {
                var sub = await (await registration()).pushManager.getSubscription();
                state = { kind: sub && Notification.permission === 'granted' ? 'on' : 'off', sub: sub };
            } catch (e) { state = { kind: 'unavailable' }; }
        }
        render();
    }

    async function enable() {
        state = { kind: 'loading' }; render();
        try {
            var perm = await Notification.requestPermission();
            if (perm !== 'granted') return refresh();
            var k = await fetch(API_BASE_URL + '/push/key');
            if (!k.ok) { state = { kind: 'unavailable' }; return render(); }
            var key = (await k.json()).publicKey;
            var reg = await registration();
            var sub = await reg.pushManager.getSubscription() ||
                await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: b64ToBytes(key) });
            var r = await post('/push/subscribe', { userId: userId(), subscription: sub.toJSON() });
            if (!r.ok) throw new Error('subscribe ' + r.status);
            await refresh();
            post('/push/test', { userId: userId() });   // confirma con una notificación de prueba
        } catch (e) {
            console.error('Push:', e);
            state = { kind: 'unavailable' }; render();
        }
    }

    async function disable() {
        try {
            var sub = state.sub || await (await registration()).pushManager.getSubscription();
            if (sub) { await post('/push/unsubscribe', { endpoint: sub.endpoint }); await sub.unsubscribe(); }
        } catch (e) { console.error('Push:', e); }
        refresh();
    }

    var HINTS = {
        loading: ['…', ''],
        needsInstall: ['', 'Para recibir avisos agregá WTD a tu pantalla de inicio (Compartir ▸ Añadir a pantalla de inicio) y abrila desde ahí.'],
        unsupported: ['', 'Este navegador no permite notificaciones.'],
        denied: ['', 'Están bloqueadas. Activalas en Ajustes ▸ Notificaciones ▸ WTD.'],
        unavailable: ['Reintentar', 'No se pudo activar. Probá de nuevo en un rato.'],
        off: ['Activar avisos', 'Cumpleaños, estrenos y recordatorios con 🔔.'],
        on: ['Desactivar', 'Activadas en este dispositivo ✓']
    };

    function render() {
        var h = HINTS[state.kind];
        els.forEach(function (el) {
            var btn = el.querySelector('button.pn-btn'), hint = el.querySelector('.pn-hint');
            btn.hidden = !h[0];
            btn.textContent = h[0];
            btn.disabled = state.kind === 'loading';
            btn.classList.toggle('on', state.kind === 'on');
            hint.textContent = h[1];
        });
    }

    function build(tag) {
        var el = document.createElement(tag);
        el.className = 'theme-setting push-setting';
        el.innerHTML = '<span class="ts-label">Notificaciones</span><button type="button" class="pn-btn"></button><div class="pn-hint"></div>';
        el.querySelector('button').addEventListener('click', function () {
            if (state.kind === 'on') disable(); else enable();
        });
        return el;
    }

    document.addEventListener('DOMContentLoaded', function () {
        // Va justo debajo de "Apariencia" en cada menú.
        document.querySelectorAll('.theme-setting').forEach(function (anchor) {
            var el = build(anchor.tagName.toLowerCase());
            anchor.parentNode.insertBefore(el, anchor.nextSibling);
            els.push(el);
        });
        if (supported) registration().catch(function () {});
        refresh();
    });
})();
