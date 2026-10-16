/* ── Cumpleaños: API + cálculo de fechas (compartido por Home, Calendario y la página Cumpleaños) ──

   Un cumpleaños es { id, name, month (1-12), day, birthYear|null, note|null }.
   No se guarda ninguna fecha concreta: la de cada año se calcula acá, por eso
   "se renueva solo" y el del año que viene ya existe. Necesita js/api.js
   (API_BASE_URL y toLocalDateStr). */

const BIRTHDAY_NOT_DEPLOYED = 'El servidor todavía no tiene la función de cumpleaños (falta actualizar el backend).';

const BirthdayApi = {
    /* true si el servidor contestó 404: significa que es una versión vieja que no conoce /birthdays. */
    notDeployed: false,

    url(id) { return `${API_BASE_URL}/birthdays${id == null ? '' : '/' + id}`; },

    /* Devuelve la lista, o null si el servidor no respondió (para distinguir
       "no hay cumpleaños" de "no pude leerlos"). */
    async list() {
        try {
            const r = await fetch(this.url());
            this.notDeployed = r.status === 404;
            if (!r.ok) throw new Error('GET ' + r.status);
            return await r.json();
        } catch (e) {
            console.error('Error cargando cumpleaños:', e);
            return null;
        }
    },
    /* create / update devuelven { ok, data } o { ok:false, error } con el mensaje del servidor. */
    async save(b, id) {
        try {
            const r = await fetch(this.url(id), {
                method: id == null ? 'POST' : 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(b)
            });
            if (r.status === 404 && id == null) { this.notDeployed = true; return { ok: false, error: BIRTHDAY_NOT_DEPLOYED }; }
            const body = await r.json().catch(() => ({}));
            return r.ok ? { ok: true, data: body } : { ok: false, error: body.error || 'No se pudo guardar' };
        } catch (e) {
            return { ok: false, error: 'No se pudo conectar con el servidor' };
        }
    },
    async remove(id) {
        try {
            const r = await fetch(this.url(id), { method: 'DELETE' });
            return r.ok || r.status === 404;
        } catch (e) { return false; }
    }
};

const BDAY_MONTHS = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre'];

function bdayIsLeap(y) { return (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0; }

/* Fecha (a mediodía local) del cumpleaños en un año dado. El 29/2 se festeja
   el 28/2 en los años no bisiestos. */
function bdayDateIn(b, year) {
    let day = b.day;
    if (b.month === 2 && day === 29 && !bdayIsLeap(year)) day = 28;
    return new Date(year, b.month - 1, day, 12, 0, 0);
}

function bdayToday() { const d = new Date(); d.setHours(12, 0, 0, 0); return d; }

/* Próxima vez que cae (hoy cuenta) y cuántos días faltan. */
function bdayNext(b, from) {
    const base = from ? new Date(from) : bdayToday();
    base.setHours(12, 0, 0, 0);
    let d = bdayDateIn(b, base.getFullYear());
    if (d < base) d = bdayDateIn(b, base.getFullYear() + 1);
    return { date: d, days: Math.round((d - base) / 86400000) };
}

/* ¿Cae el día `dayStr` ("YYYY-MM-DD")? Sirve para dibujarlo en cualquier año. */
function bdayOnDay(b, dayStr) {
    const d = new Date(dayStr + 'T12:00:00');
    const c = bdayDateIn(b, d.getFullYear());
    return c.getMonth() === d.getMonth() && c.getDate() === d.getDate();
}

/* Años que cumple en la fecha dada (null si no se cargó el año de nacimiento). */
function bdayAge(b, date) {
    return b.birthYear ? date.getFullYear() - b.birthYear : null;
}

function bdayAgeText(b, date) {
    const age = bdayAge(b, date);
    return age == null ? 'Cumpleaños' : (age <= 0 ? 'Nacimiento' : `Cumple ${age} ${age === 1 ? 'año' : 'años'}`);
}

/* Todos los cumpleaños que caen entre dos fechas (inclusive), con su fecha ya calculada. */
function bdaysBetween(list, from, to) {
    const out = [];
    const fromD = new Date(from); fromD.setHours(12, 0, 0, 0);
    const toD = new Date(to); toD.setHours(12, 0, 0, 0);
    for (let y = fromD.getFullYear(); y <= toD.getFullYear(); y++) {
        list.forEach(b => {
            const d = bdayDateIn(b, y);
            if (d >= fromD && d <= toD) out.push({ b, date: d, dateStr: toLocalDateStr(d) });
        });
    }
    return out.sort((a, c) => a.date - c.date || a.b.name.localeCompare(c.b.name));
}

function bdayDaysText(days) {
    if (days === 0) return 'Hoy';
    if (days === 1) return 'Mañana';
    if (days < 14) return `En ${days} días`;
    if (days < 60) return `En ${Math.round(days / 7)} semanas`;
    return `En ${Math.round(days / 30)} meses`;
}

/* Minúsculas y sin tildes, para buscar "maria" y encontrar "María". */
function bdayNorm(s) {
    return String(s || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
}
