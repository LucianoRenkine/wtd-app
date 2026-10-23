/* ── Catálogo compartido: categorías y prioridades ── */

/* Cada categoría es un bloque de color sólido, sin ícono — la lista de
   categorías la maneja el backend (GET /tasks/categories), acá solo se
   reescribe el color con una paleta más sobria que la de la base. */
const CATEGORY_STYLE = {
    'Estudio':  { color: '#4a72a8' },
    'Trabajo':  { color: '#2e7d5b' },
    'Personal': { color: '#a63d63' },
    'Final':    { color: '#a78bda', confirmedColor: '#6d28d9' }
};
const CATEGORY_FALLBACK = { color: '#7a7389' };

/* En oscuro las mismas categorías se aclaran para sostener el contraste. */
const CATEGORY_STYLE_DARK = {
    'Estudio':  { color: '#7fa6dc' },
    'Trabajo':  { color: '#4fbf8f' },
    'Personal': { color: '#e0719a' },
    'Final':    { color: '#b9a0ee', confirmedColor: '#9d7bf5' }
};
const CATEGORY_FALLBACK_DARK = { color: '#a09bad' };
const isDarkTheme = () => document.documentElement.getAttribute('data-theme') === 'dark';

/* "Final" tiene dos tonos: violeta suave y, si la tarea está confirmada,
   violeta fuerte. Las demás categorías ignoran `confirmed`. */
function catStyle(category, confirmed) {
    const name = typeof category === 'string' ? category : (category && category.name);
    const dark = isDarkTheme();
    const st = (dark ? CATEGORY_STYLE_DARK : CATEGORY_STYLE)[name] || (dark ? CATEGORY_FALLBACK_DARK : CATEGORY_FALLBACK);
    return confirmed && st.confirmedColor ? { color: st.confirmedColor } : st;
}

/* Prioridad: las claves son el enum del backend (Task.Priority).
   NONE es la ausencia de prioridad — default de toda tarea nueva, no se
   marca de ninguna forma especial. LOW se ve atenuada. MEDIUM es el punto
   neutro (ni negrita ni atenuada). HIGH va en negrita y se marca con 🔥
   — la urgencia ya no depende de una categoría "Urgente". */
const PRIORITY_STYLE = {
    'NONE':   { label: 'Sin prioridad', mark: '',  bar: 2, weight: 500, opacity: 1,    color: '#a49dae' },
    'LOW':    { label: 'Baja',          mark: '',  bar: 2, weight: 500, opacity: 0.62, color: '#837e90' },
    'MEDIUM': { label: 'Media',         mark: '',  bar: 2, weight: 500, opacity: 1,    color: '#45424f' },
    'HIGH':   { label: 'Alta',          mark: '🔥', bar: 4, weight: 700, opacity: 1,    color: '#c25a34' }
};
const PRIORITY_ORDER = ['NONE', 'LOW', 'MEDIUM', 'HIGH'];

const PRIORITY_COLOR_DARK = { 'NONE': '#767187', 'LOW': '#9a95a8', 'MEDIUM': '#cfcbd9', 'HIGH': '#f08a5d' };

function prioStyle(priority) {
    const key = (priority || 'NONE').toUpperCase();
    const st = PRIORITY_STYLE[key] || PRIORITY_STYLE['NONE'];
    return isDarkTheme() ? { ...st, color: PRIORITY_COLOR_DARK[key] || st.color } : st;
}
