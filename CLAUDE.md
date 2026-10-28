# WTD ("What To Do") — contexto del proyecto

App personal para dos perfiles (compartida): calendario de tareas/eventos, watchlist de series y
películas, cumpleaños, suscripciones y tareas repetidas, con avisos push al iPhone (PWA).
Backend Spring Boot + PostgreSQL; frontend HTML/JS vanilla sin build. Todo en español rioplatense.

**Leé esto antes de explorar el código**: casi todo lo que hace falta saber está en estos archivos.
Si algo cambia (endpoint, página, convención), actualizalos en el mismo commit.

@docs/context/arquitectura.md
@docs/context/funcionalidades.md
@docs/context/convenciones.md

## Lo más importante (resumen)

- **Cada release a prod = una novedad** en `backend/wtd-app/src/main/resources/changelog.json`
  (ver "Novedades" en convenciones.md). No hace falta que lo pida.
- **Commits y pushes separados por temática**, en español, estilo `feat:` / `fix:` / `docs:`.
  Commitear/pushear solo cuando lo pide.
- Si el front depende de un endpoint nuevo: **primero el backend, verificar en prod, después el front**.
- Fechas en el front: **nunca `toISOString()`**, usar `toLocalDateStr()` (GMT-3).
- Al levantar el proyecto en local: backend `:8080/api`, front con `python3 -m http.server 4321 --directory frontend`.
