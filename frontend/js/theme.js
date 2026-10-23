/* ── Apariencia: claro / oscuro / automático ──
   Se carga en el <head> (antes de los estilos) para que no haya parpadeo.
   La preferencia vive en localStorage ('wtdTheme'); el valor ya resuelto
   queda en <html data-theme="light|dark">. */
(function () {
    var KEY = 'wtdTheme', root = document.documentElement;
    var mq = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : { matches: false };

    function pref() { try { return localStorage.getItem(KEY) || 'light'; } catch (e) { return 'light'; } }
    function resolve(p) { return p === 'dark' || (p === 'auto' && mq.matches) ? 'dark' : 'light'; }
    function apply() {
        var t = resolve(pref());
        root.setAttribute('data-theme', t);
        // Color de la barra del sistema (iPhone / PWA) acorde al tema.
        document.addEventListener('DOMContentLoaded', function () {
            var m = document.querySelector('meta[name="theme-color"]');
            if (m) m.setAttribute('content', t === 'dark' ? '#0c121c' : '#fbfbfd');
        });
    }
    apply();

    window.WtdTheme = {
        pref: pref,
        isDark: function () { return root.getAttribute('data-theme') === 'dark'; },
        set: function (p) {
            try { localStorage.setItem(KEY, p); } catch (e) {}
            // Varias pantallas pintan colores desde JS: recargar es lo más simple y seguro.
            location.reload();
        }
    };

    if (mq.addEventListener) mq.addEventListener('change', function () {
        if (pref() === 'auto') location.reload();
    });

    function build(tag) {
        var el = document.createElement(tag);
        el.className = 'theme-setting';
        var cur = pref();
        el.innerHTML = '<span class="ts-label">Apariencia</span><div class="ts-seg" role="group" aria-label="Apariencia">' +
            [['light', 'Claro'], ['dark', 'Oscuro'], ['auto', 'Auto']].map(function (o) {
                return '<button type="button" data-t="' + o[0] + '" class="' + (cur === o[0] ? 'on' : '') + '">' + o[1] + '</button>';
            }).join('') + '</div>';
        el.addEventListener('click', function (e) {
            var b = e.target.closest('button[data-t]');
            if (b && b.dataset.t !== pref()) WtdTheme.set(b.dataset.t);
        });
        return el;
    }

    document.addEventListener('DOMContentLoaded', function () {
        // Barra lateral de escritorio (Home / Calendario): va justo arriba de "Cerrar sesión".
        var out = document.querySelector('.side-nav .side-nav-logout');
        if (out) out.parentNode.insertBefore(build('div'), out);

        // Menús en lista (drawer del celular, Watchlist, Cumpleaños): antes del separador de salir.
        document.querySelectorAll('nav ul').forEach(function (list) {
            var logout = list.querySelector('.logout-link');
            if (!logout) return;
            var anchor = logout.closest('li');
            var before = anchor.previousElementSibling && anchor.previousElementSibling.tagName === 'HR' ? anchor.previousElementSibling : anchor;
            list.insertBefore(build('li'), before);
        });
    });
})();
