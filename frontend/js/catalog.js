/* ── Catálogo compartido: categorías y prioridades ── */

/* Colores e íconos por nombre de categoría. La paleta del backend es vieja
   (flat-ui saturado), así que el front la reescribe con tonos sobrios. */
const CATEGORY_STYLE = {
    'Estudio':  { color: '#4a72a8', icon: '📚' },
    'Trabajo':  { color: '#2e7d5b', icon: '💼' },
    'Personal': { color: '#a63d63', icon: '🌿' },
    'Urgente':  { color: '#c25a34', icon: '🔥' }
};
const CATEGORY_FALLBACK = { color: '#7a7389', icon: '•' };

function catStyle(category) {
    const name = typeof category === 'string' ? category : (category && category.name);
    return CATEGORY_STYLE[name] || CATEGORY_FALLBACK;
}

/* Prioridad: las claves son el enum del backend (Task.Priority).
   HIGH -> barra gruesa + "!!"   MEDIUM -> barra fina   LOW -> atenuada */
const PRIORITY_STYLE = {
    'HIGH':   { label: 'Alta',  mark: '!!', bar: 4, weight: 700, opacity: 1,    color: '#c25a34' },
    'MEDIUM': { label: 'Media', mark: '',   bar: 2, weight: 600, opacity: 1,    color: '#45424f' },
    'LOW':    { label: 'Baja',  mark: '',   bar: 2, weight: 500, opacity: 0.62, color: '#837e90' }
};
const PRIORITY_ORDER = ['HIGH', 'MEDIUM', 'LOW'];

function prioStyle(priority) {
    return PRIORITY_STYLE[(priority || 'MEDIUM').toUpperCase()] || PRIORITY_STYLE['MEDIUM'];
}
