# Convenciones y forma de trabajar del usuario

## Novedades (release notes) — hacerlo sin que lo pida
Cada vez que se pushea una release a prod, publicar la novedad en
`backend/wtd-app/src/main/resources/changelog.json` (se sirve desde el backend → requiere deploy).
- **Formato** de cada entrada: `id` (`YYYY-MM-DD-slug`, único, no se reutiliza), `date` (hoy), `title`, `summary`
  (1 frase; es el cuerpo del push), `sections[]` con `icon` (emoji), `title` e `items[]` (frases cortas, voseo).
- La más nueva **arriba**. Se insertan sin reformatear las existentes (el diff debe ser solo líneas agregadas).
- **Cada entrada = 1 push al iPhone por dispositivo**, una sola vez, y solo si hoy está entre `date` y `date + 7 días`.
  Se mandan de la más vieja a la más nueva (la de más abajo en el archivo llega primero). Editar el texto de una
  entrada ya avisada no la vuelve a avisar.
- Se pueden publicar varias en un push: lo habitual es **2–3 novedades temáticas** (no una por commit). Si el
  usuario prefiere un solo aviso, juntar todo en una entrada con varias secciones.
- Para **retocar el texto de una novedad ya publicada** sin avisar de nuevo: cambiar solo `items` (no `id`, `date`,
  `title` ni `summary`). El aviso se registra por `changelog:<id>:<dispositivo>`, el texto no cuenta. Cambiar el `id`
  o la `date` sí re-avisa. Solo lo recibe una vez cualquier dispositivo que active los avisos después.
- Va en un **commit propio** (`feat: novedades de …`) que se pushea al final, cuando lo anterior ya está en prod.
- Tono: lo que el usuario puede hacer ahora, no detalles técnicos. Sin mencionar archivos ni endpoints.

## Git
- **Commits separados por temática** y pusheados por separado. Mensajes en español, `feat:` / `fix:` / `docs:`,
  cuerpo corto explicando el qué y el porqué. Trailer `Co-Authored-By` según indique el entorno.
- Commitear y pushear **solo cuando el usuario lo pide**. Cuando lo pide, no preguntar de nuevo.
- Hay trabajo propio sin commitear del usuario mezclado en el working tree: **no mezclarlo** en commits de otra
  temática. Para separar cambios dentro de un mismo archivo: revertir con `git apply -R --unidiff-zero --recount`
  los hunks propios, commitear el resto y restaurar el archivo (probado en `calendar.html`).
- Orden de push cuando hay dependencia: backend → verificar `curl` 200 en prod → front → novedades.
- Push a `main` desde un commit puntual: `git push origin <sha>:refs/heads/main`.

## Código
- Todo en **español**: UI, comentarios, mensajes de error. Voseo rioplatense ("Agregá", "Tocá", "Probá").
- Comentarios que explican el **porqué**, con bloques `/* ── Título ── */` en el front y Javadoc corto en el backend.
- Fechas: guardar/manejar `"YYYY-MM-DD"` locales con `toLocalDateStr()`; **nunca** `toISOString()` (en GMT-3 da el
  día siguiente después de las 21hs).
- Antes de armar algo nuevo, copiar el patrón de lo existente: lista compartida → mirar `Birthday` + `cumpleanos.html`.
  Checklist de una página/feature nueva: entidad+repo+controller → módulo `js/*.js` → página → link en todos los
  menús → integración en calendario/Home si corresponde → novedad.
- Jackson + Lombok `@AllArgsConstructor`: un campo ausente en el JSON llega `null` y se salta el default; usar el
  patrón `applyDefaults()` de `Task` o normalizar en `clean()`.
- UI sobria: el usuario sacó emojis decorativos del calendario (🔁). Pocos emojis, solo donde aportan (🎂, 💳);
  usar variables de `theme.css` y chequear modo oscuro.
- Reglas de negocio pequeñas viven en el front (cálculo de fechas) para no guardar ocurrencias.

## Cómo le gusta que se trabaje
- Quiere **ver el resultado**: levantar el front/backend local y pasar el link antes de dar algo por terminado.
- Respuestas cortas en español, con lo que se hizo y qué quedó sin verificar. Ofrecer el siguiente paso en una línea.
- Acciones con efecto afuera (push a prod, notificaciones al iPhone) solo cuando las pide; avisar qué va a pasar
  (por ejemplo: "esto manda N pushes").

## Gotchas de la máquina (macOS)
- `sed -i ''` (con comillas vacías). `psql -c` no interpola `-v`: pasar el SQL por stdin.
- `pkill` no mata `gradlew bootRun`: usar `kill $(lsof -ti :8080)`; después esperar a que levante.
- Para generar un hash BCrypt local: `htpasswd -bnBC 10 "" <clave>` (cambiar `$2y` por `$2a`).
