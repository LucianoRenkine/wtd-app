# Funcionalidades (cómo funciona cada una)

## Calendario (`frontend/calendar.html`)
- Vista inicial "rodante": semana actual + 3 siguientes (`rollingView`, `ROLLING_WEEKS`); al navegar con ‹ › pasa a la
  grilla mensual. Doble click en un día abre la vista de día.
- Filtros de categoría y prioridad como `<select>`, más "Compartidas".
- Cumpleaños: marca 🎂 junto al número del día (no ocupa renglón). Suscripciones/tareas repetidas: píldoras
  (`buildRecurringPill`) antes de las tareas; tocarlas abre un cuadro con "Cancelar solo esta vez" / "Ver / editar".
- Pasado y días de otro mes se ven atenuados. Estilo sobrio, "calmo" en oscuro.

## Cumpleaños (`cumpleanos.html`, `js/birthdays.js`)
No se guarda una fecha: se guarda día+mes(+año) y cada año se calcula (`bdayOnDay`, `bdayNext`). 29/2 cae el 28 en
años no bisiestos. Es la plantilla de la que salió Suscripciones.

## Suscripciones y tareas repetidas (`suscripciones.html`, `js/recurring.js`, `Recurring`)
- **Privadas por defecto**: cada una tiene `ownerId` y `shared` (false). `GET /recurrings?userId=` devuelve las propias,
  las compartidas y las que no tienen dueño (anteriores a este cambio, las ven todos hasta que alguien las edita y
  pasa a ser su dueño). Solo el dueño puede cambiar `shared` (botón Compartir/Compartida en cada fila y en el alta).
- Un solo modelo con `kind` = `SUBSCRIPTION` | `TASK`, `frequency` = `WEEKLY|MONTHLY|YEARLY`, `startDate`,
  `endDate` (null = tiempo indeterminado), `amount`/`currency` (solo suscripciones; ARS/USD/EUR), `note`,
  `skippedDates`.
- `frequency` = `MONTHLY_WEEKDAY` es "el primer sábado de cada mes" (`weekOfMonth` 1–4, 5 = último; `weekday`
  0=domingo … 6=sábado como `getDay()`). En ese caso `startDate` es solo el "desde cuándo".
- **No se guardan ocurrencias**: `recOnDay(r, "YYYY-MM-DD")` decide si cae ese día. Mensual día 31 cae el último día
  de meses cortos; anual 29/2 cae el 28.
- `skippedDates`: fechas canceladas "solo una vez" (coma-separadas). `recOnDay` las excluye, así que también
  mueven "próxima renovación". Se restauran desde el modal de edición ("Veces canceladas").
- La página tiene dos pestañas (Suscripciones / Tareas repetidas), resumen por mes/año por moneda, buscador, edición
  en modal. `?k=TASK&q=nombre` abre la pestaña y la búsqueda (lo usa el calendario).
- Las suscripciones muestran 💳 + costo; las tareas repetidas solo el nombre (el usuario pidió sacar el 🔁).

## Watchlist (`watchlist.html`)
Lista compartida con pestañas Viendo / Quiero ver / Próximas / Vistas; póster y autocompletado vía TMDB (se conecta
desde el botón TMDB; pegar un link de TMDB trae todo); calendario de estrenos por capítulo; selección múltiple.

## Push al iPhone (`js/push.js`, `sw.js`, `NotificationJob`)
Se activa desde Menú ▸ Notificaciones con la app instalada en la pantalla de inicio. Recordatorios (campanita en
cada tarea), cumpleaños, estrenos y novedades.

## Novedades (`novedades.html`, `changelog.json`)
Historial de lo nuevo en la app. La campanita 🔔 del header muestra un punto cuando hay algo sin ver. Detalle de
cómo publicar en convenciones.md.
