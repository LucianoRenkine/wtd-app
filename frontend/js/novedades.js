/* ── Campanita de Novedades (arriba a la derecha, al lado del perfil) ──
   Lleva a novedades.html y muestra un punto si hay una novedad que todavía no
   se vio (compara con localStorage 'wtdSeenChangelog', que guarda la propia
   página Novedades). Depende de api.js (API_BASE_URL). */
(function () {
    if (typeof API_BASE_URL === 'undefined') return;

    var BELL = '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>';

    function mount() {
        var a = document.createElement('a');
        a.className = 'bell-btn';
        a.href = 'novedades.html';
        a.title = 'Novedades';
        a.setAttribute('aria-label', 'Novedades');
        a.innerHTML = BELL;
        if (/novedades\.html$/.test(location.pathname)) a.classList.add('active');

        var chip = document.querySelector('header .profile-chip');
        if (chip) {
            chip.parentNode.insertBefore(a, chip);
        } else {
            // Páginas sin perfil en el header: ocupa el hueco vacío de la derecha.
            var slot = document.querySelector('header > div:last-child');
            if (!slot || slot.children.length) return null;
            slot.className = 'header-side right';
            slot.style.cssText = 'display:flex;justify-content:flex-end;align-items:center';
            slot.appendChild(a);
        }
        return a;
    }

    document.addEventListener('DOMContentLoaded', async function () {
        var bell = mount();
        if (!bell || bell.classList.contains('active')) return;
        try {
            var r = await fetch(API_BASE_URL + '/changelog');
            if (!r.ok) return;
            var list = await r.json();
            if (!list.length) return;
            var seen = null;
            try { seen = localStorage.getItem('wtdSeenChangelog'); } catch (e) {}
            if (seen === list[0].id) return;
            var dot = document.createElement('span');
            dot.className = 'bell-dot';
            bell.appendChild(dot);
            bell.title = 'Novedades · hay algo nuevo';
        } catch (e) { /* sin conexión: sin punto */ }
    });
})();
