# Arquitectura

## Stack y carpetas
- `backend/wtd-app/` — Spring Boot 4.0 (Java 17, Gradle, Lombok, JPA/Hibernate). Context path `/api`, puerto `8080` (`PORT`).
  Paquete `org.example.wtdapp`: `controller/`, `entity/`, `repository/`, `service/`, `scheduler/`.
  Esta es la carpeta que usa el `Dockerfile` de la raíz (solo construye el backend).
- `backend/demo/` — scaffold de Gradle que no parece usarse (verificar antes de tocarlo).
- `frontend/` — páginas estáticas, sin framework ni build. Cada `.html` trae su `<style>` y `<script>` inline;
  lo compartido está en `js/` y `css/theme.css`.
- Raíz: `seed.sql`, `seed_watchlist.sql`, `wtd-backup.dump` (datos de desarrollo/respaldo).
- Hay ramas viejas (`feat/watchlist`, `watchlist`); el trabajo va directo a `main`.

## Base de datos
- PostgreSQL. `spring.jpa.hibernate.ddl-auto=update`: **una entidad/columna nueva se crea sola** (también en prod),
  no hay migraciones. Credenciales y URL salen de `DATABASE_URL/USERNAME/PASSWORD`; hay fallback local en
  `application.properties`.
- Local: base `wtd-db` en `localhost:5432`, usuario `postgres`.

## Entidades y API (todo bajo `/api`)
| Entidad | Endpoint | Notas |
|---|---|---|
| `User` | `/users`, `POST /users/{id}/login` | Login por perfil + contraseña (BCrypt). 3 usuarios: Administrador, Ju, Lulo |
| `Task`, `Category` | `/tasks`, `/tasks/categories`, compartir con `/tasks/{id}/share/{userId}` | Por usuario, se pueden compartir. `itemType` TASK/EVENT, categorías Estudio/Trabajo/Personal/Final, prioridad, `reminder` (campanita), `confirmed` |
| `Birthday` | `/birthdays` | Lista **compartida**; día+mes(+año opcional) |
| `WatchlistItem` | `/watchlist` | Lista **compartida**; TMDB desde el front |
| `Recurring` | `/recurrings?userId=` | **Privada de su dueño** (`ownerId`) salvo `shared=true`; suscripciones y tareas repetidas (ver funcionalidades.md) |
| `PushSubscription`, `SentNotification` | `/push/*` (`key`, `status`, `subscribe`, `unsubscribe`, `test`) | Web Push con VAPID |
| (changelog) | `GET /changelog` | Lee `resources/changelog.json` |

Patrón de las listas (Birthday compartida; Recurring con dueño y `shared`): `@Entity` + `JpaRepository` + controller con
`validate()` (devuelve mensaje en español → 400 `{error}`) y `clean()` (copia solo los campos editables).
`@CrossOrigin(origins="*")`. DELETE devuelve 204.

## Jobs (`scheduler/`)
- `NotificationJob` — corre cada minuto, decide qué push mandar. Cada aviso lleva una clave en `sent_notifications`
  → nunca se manda dos veces. Zona `America/Argentina/Buenos_Aires`. Tipos: recordatorios de tareas (15 min antes /
  9:00 si es todo el día), cumpleaños (9:00), estrenos de Watchlist (9:00), **novedades** (ver convenciones.md).
- `EventAutoCompleteJob` — cada 5 min completa solos los Eventos (no las Tareas) cuyo horario ya terminó.
- Push desactivado si faltan `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY`.

## Frontend
Páginas: `login`, `index` (Home), `calendar`, `agenda` ("Mi Agenda", no está en el menú), `watchlist`, `cumpleanos`, `suscripciones`, `novedades`.
- Auth: `localStorage.wtdAuth = {userId, user}`; cada página arranca con un guard que redirige a `login.html`.
- `js/api.js` — cliente de API de tareas/usuarios + `toLocalDateStr()` + `API_BASE_URL`. **En `localhost` apunta a
  `http://localhost:8080/api`; en cualquier otro host, a producción** (Railway). Se puede forzar con
  `localStorage.wtdApiBase`.
- Un módulo JS por feature de lista: `js/birthdays.js` (`BirthdayApi`, `bday*`), `js/recurring.js` (`RecurringApi`, `rec*`).
  Cada uno tiene `list/save/remove`, devuelve `null`/`{ok:false,error}` ante fallas y maneja "backend viejo (404)".
- `js/catalog.js` colores de categorías/prioridades; `js/theme.js` claro/oscuro/auto (`data-theme` en `<html>`);
  `js/push.js` + `sw.js` push (el service worker no cachea nada); `js/novedades.js` campanita con punto de "nuevo".
- `css/theme.css` — variables (`--ink`, `--accent`, `--surface`, `--muted`…) para claro y oscuro (`:root[data-theme="dark"]`).
  Colores nuevos: usar las variables y revisar el modo oscuro.
- Cache-busting: los includes llevan `?v=N` (`api.js?v=5`, `theme.css?v=9`…). Al cambiar un js/css compartido,
  subir el `N` en todas las páginas que lo incluyen. Los módulos nuevos arrancan en `?v=1`.
- Menú lateral: hay que sumar cada página nueva a **todos** los menús (`index` ×2, `calendar` ×2, `novedades`,
  `watchlist`, `cumpleanos`, `suscripciones`).
- PWA: `manifest.webmanifest`, íconos en `icons/`.

## Local y producción
- Local: Postgres corriendo; backend `cd backend/wtd-app && ./gradlew bootRun`; front
  `python3 -m http.server 4321 --directory frontend` (está en `.claude/launch.json`).
  Los usuarios del seed local tienen hashes falsos (`hash_temporal…`): no se puede loguear. En la base local de
  este equipo, **Lulo** tiene contraseña `test1234` (solo local; prod tiene la real).
- Prod backend: Railway, `https://wtd-app-production.up.railway.app/api`. Se despliega solo al pushear a `main`
  (tarda ~1–2 min; verificable con `curl` al endpoint nuevo).
- Prod frontend: **no está documentado dónde se hospeda** — confirmar con el usuario antes de asumir.
