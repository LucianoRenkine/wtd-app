/* ── Suscripciones y tareas periódicas: API + cálculo de fechas (compartido por la página y el Calendario) ──

   Un item es { id, kind: 'SUBSCRIPTION'|'TASK', name, amount, currency,
   frequency: 'WEEKLY'|'MONTHLY'|'YEARLY', startDate, endDate|null, note,
   shared, ownerId, weekOfMonth, weekday }. Cada una es privada de su dueño hasta que se marca `shared`.
   No se guardan las ocurrencias: salen de startDate + frequency, y sin
   endDate duran por tiempo indeterminado. Necesita js/api.js (API_BASE_URL). */

const RECURRING_NOT_DEPLOYED = 'El servidor todavía no tiene esta función (falta actualizar el backend).';
const REC_FREQ = { WEEKLY: 'Cada semana', MONTHLY: 'Cada mes (mismo día)', MONTHLY_WEEKDAY: 'Cada mes (ej. primer sábado)', YEARLY: 'Cada año' };
const REC_NTH = { 1: 'primer', 2: 'segundo', 3: 'tercer', 4: 'cuarto', 5: 'último' };
const REC_WEEKDAYS = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'];
const REC_MONTHS = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

const RecurringApi = {
    notDeployed: false,

    url(id) { return `${API_BASE_URL}/recurrings${id == null ? '' : '/' + id}`; },

    /* Devuelve la lista, o null si el servidor no respondió. */
    async list() {
        try {
            const r = await fetch(this.url() + (currentUserId ? '?userId=' + currentUserId : ''));
            this.notDeployed = r.status === 404;
            if (!r.ok) throw new Error('GET ' + r.status);
            return await r.json();
        } catch (e) {
            console.error('Error cargando suscripciones:', e);
            return null;
        }
    },
    async save(item, id) {
        try {
            const r = await fetch(this.url(id), {
                method: id == null ? 'POST' : 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...item, ownerId: currentUserId })
            });
            if (r.status === 404 && id == null) { this.notDeployed = true; return { ok: false, error: RECURRING_NOT_DEPLOYED }; }
            const body = await r.json().catch(() => ({}));
            return r.ok ? { ok: true, data: body } : { ok: false, error: body.error || 'No se pudo guardar' };
        } catch (e) {
            return { ok: false, error: 'No se pudo conectar con el servidor' };
        }
    },
    async remove(id) {
        try {
            const r = await fetch(this.url(id), { method: 'DELETE' });
            return r.ok;
        } catch (e) { return false; }
    }
};

/* Días cancelados de a uno ("YYYY-MM-DD,YYYY-MM-DD") como arreglo. */
function recSkipped(r) {
    return r.skippedDates ? r.skippedDates.split(',').filter(Boolean) : [];
}
function recSkipValue(list) {
    return list.length ? list.slice().sort().join(',') : null;
}

/* ¿Puede este perfil cambiar si es compartida? El dueño, o cualquiera si es anterior al dueño. */
function recIsOwner(r) { return r.ownerId == null || r.ownerId === currentUserId; }

function recParse(dayStr) {
    const [y, m, d] = dayStr.split('-').map(Number);
    return { y, m, d };
}
function recDaysInMonth(y, m) { return new Date(y, m, 0).getDate(); }

/* ¿Cae el día `dayStr` ("YYYY-MM-DD")? Un día 31 mensual cae el último día de los meses más cortos. */
function recOnDay(r, dayStr) {
    if (dayStr < r.startDate) return false;
    if (r.endDate && dayStr > r.endDate) return false;
    if (recSkipped(r).includes(dayStr)) return false;
    const s = recParse(r.startDate), t = recParse(dayStr);
    if (r.frequency === 'WEEKLY') {
        const diff = Date.UTC(t.y, t.m - 1, t.d) - Date.UTC(s.y, s.m - 1, s.d);
        return Math.round(diff / 86400000) % 7 === 0;
    }
    if (r.frequency === 'MONTHLY_WEEKDAY') {
        // "El primer sábado": el día de la semana coincide y es la semana N del mes (5 = la última).
        if (new Date(t.y, t.m - 1, t.d).getDay() !== r.weekday) return false;
        return r.weekOfMonth === 5 ? t.d + 7 > recDaysInMonth(t.y, t.m) : Math.ceil(t.d / 7) === r.weekOfMonth;
    }
    if (r.frequency === 'MONTHLY') {
        return t.d === Math.min(s.d, recDaysInMonth(t.y, t.m));
    }
    return t.m === s.m && t.d === Math.min(s.d, recDaysInMonth(t.y, s.m));
}

/* Próxima vez que cae (hoy cuenta) y cuántos días faltan; null si ya terminó. */
function recNext(r, from) {
    const base = from ? new Date(from) : new Date();
    base.setHours(12, 0, 0, 0);
    for (let i = 0; i <= 400; i++) {
        const d = new Date(base); d.setDate(d.getDate() + i);
        const str = toLocalDateStr(d);
        if (r.endDate && str > r.endDate) return null;
        if (recOnDay(r, str)) return { date: d, dateStr: str, days: i };
    }
    return null;
}

/* Cuánto cuesta por mes (semanal ×52/12, anual ÷12). */
function recMonthlyAmount(r) {
    const a = r.amount || 0;
    return r.frequency === 'WEEKLY' ? a * 52 / 12 : (r.frequency === 'YEARLY' ? a / 12 : a);
}

function recMoney(amount, currency) {
    if (amount == null) return '';
    const n = Number(amount).toLocaleString('es-AR', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
    return `${currency === 'USD' ? 'US$' : (currency === 'EUR' ? '€' : '$')}${n}`;
}

/* "Cada lunes", "El 15 de cada mes", "Cada 3 de marzo". */
function recWhenText(r) {
    const s = recParse(r.startDate);
    if (r.frequency === 'WEEKLY') return `Cada ${REC_WEEKDAYS[new Date(s.y, s.m - 1, s.d).getDay()]}`;
    if (r.frequency === 'MONTHLY_WEEKDAY') {
        const nth = REC_NTH[r.weekOfMonth] || '';
        return `El ${nth} ${REC_WEEKDAYS[r.weekday]} de cada mes`;
    }
    if (r.frequency === 'MONTHLY') return `El ${s.d} de cada mes`;
    return `Cada ${s.d} de ${REC_MONTHS[s.m - 1]}`;
}

function recDaysText(days) {
    if (days === 0) return 'Hoy';
    if (days === 1) return 'Mañana';
    if (days < 14) return `En ${days} días`;
    return `En ${Math.round(days / 7)} semanas`;
}

/* Emoji y texto para la píldora del calendario. */
function recPillText(r) {
    return r.kind === 'SUBSCRIPTION' ? `💳 ${r.name} ${recMoney(r.amount, r.currency)}`.trim() : r.name;
}

function recNorm(s) {
    return String(s || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
}
