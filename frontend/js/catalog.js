/* ── Catálogo compartido: categorías y prioridades ── */

/* Cada categoría es un bloque de color sólido, sin ícono — la lista de
   categorías la maneja el backend (GET /tasks/categories), acá solo se
   reescribe el color con una paleta más sobria que la de la base. */
const CATEGORY_STYLE = {
    'Estudio':  { color: '#4a72a8' },
    'Trabajo':  { color: '#2e7d5b' },
    'Personal': { color: '#a63d63' }
};
const CATEGORY_FALLBACK = { color: '#7a7389' };

function catStyle(category) {
    const name = typeof category === 'string' ? category : (category && category.name);
    return CATEGORY_STYLE[name] || CATEGORY_FALLBACK;
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

function prioStyle(priority) {
    return PRIORITY_STYLE[(priority || 'NONE').toUpperCase()] || PRIORITY_STYLE['NONE'];
}
