# Technical Reports — Scontrol Ingeniería

API REST para registrar los informes técnicos de mantenimiento de equipos (radiocontrol,
video, anticolisión, pesaje, antifatiga y seguridad para puentes grúa) y generar el
documento final en PDF/Word. Reemplaza el llenado manual en Excel.

## Stack

- Java 21, Spring Boot 4, Maven
- Spring Security + JWT (OAuth2 Resource Server)
- Spring Data JPA, PostgreSQL, Flyway
- Tests de integración con Testcontainers

## Requisitos

- JDK 21+
- Docker (base de datos local y tests)

## Ejecutar en local

```bash
docker compose up -d

JWT_SECRET=<mínimo 32 caracteres> \
ADMIN_EMAIL=<email> \
ADMIN_PASSWORD=<clave> \
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`. El login es `POST /api/auth/login` con
`{"email": "...", "password": "..."}`, y el resto de endpoints requiere
`Authorization: Bearer <token>`.

## Variables de entorno

| Variable | Obligatoria | Descripción |
|---|---|---|
| `JWT_SECRET` | Sí | Clave para firmar los JWT (HS256, mínimo 32 caracteres) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Solo la primera vez | Usuario inicial; se crea si la tabla `users` está vacía |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | No | Conexión a PostgreSQL; por defecto apunta al contenedor de `compose.yaml` |

## Tests

```bash
./mvnw test
```

Requiere Docker activo: los tests levantan un PostgreSQL real con Testcontainers.

## Documentación

- [`DESIGN.md`](DESIGN.md): modelo de datos, endpoints, reglas de negocio y fases de desarrollo.
- [`CLAUDE.md`](CLAUDE.md): convenciones de código y de trabajo en el proyecto.
