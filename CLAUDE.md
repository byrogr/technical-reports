# CLAUDE.md

Contexto e instrucciones para Claude Code en este proyecto. Léelo antes de generar o
modificar código.

## Proyecto

Sistema de Informes Técnicos de mantenimiento para Scontrol Ingeniería S.A.C.
El diseño completo (modelo de datos, endpoints, fases de desarrollo) está en `DESIGN.md`
en esta misma raíz — léelo también antes de implementar cualquier fase.

## Stack

- Java 21+, Spring Boot 4
- Spring Security + JWT vía `spring-boot-starter-security-oauth2-resource-server` (Nimbus JOSE, sin librerías JWT extra como jjwt)
- Spring Data JPA
- PostgreSQL
- Flyway (migraciones)
- Maven
- Lombok (reducir boilerplate) y MapStruct (mapeo Entity <-> DTO)
- OpenPDF para generar el informe en PDF (solo PDF, sin Word) — **no usar iText** (licencia AGPL)
- Testcontainers (PostgreSQL) para tests de integración — no usar H2

## Convenciones de código (obligatorias)

**Idioma:** todo lo que se convierte en código va en inglés — nombres de clases,
tablas, columnas, endpoints, variables, métodos. Los comentarios pueden ir en español.

**Documentación:** toda clase creada lleva un Javadoc básico (no extenso) en la
cabecera, con autor. No se documenta cada método, solo la clase.

```java
/**
 * Servicio encargado de la gestión de informes técnicos.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public class TechnicalReportService {
    // ...
}
```

*Nota:* el `@author` usa el formato `Nombre - correo`, sin `<...>`: Javadoc interpretaría
los signos como HTML y el doclint fallaría.

**Lombok y MapStruct:**
- Entidades JPA: `@Getter`, `@Setter` y `@NoArgsConstructor`, con `@Setter(AccessLevel.NONE)`
  en el `id` y en campos gestionados por la BD. **No usar `@Data`, `@ToString` ni
  `@EqualsAndHashCode` en entidades** (disparan cargas lazy y rompen el `equals` de Hibernate).
- Servicios, controllers y componentes: inyección por constructor con `@RequiredArgsConstructor`;
  logs con `@Slf4j`.
- DTOs y `@ConfigurationProperties`: `record` de Java, sin Lombok.
- Mapeos: interfaces MapStruct en `mapper/` con `@Mapper(uses = StringMapper.class)` (recorta
  espacios en los String). El `pom.xml` configura `componentModel=spring`, inyección por
  constructor y `unmappedTargetPolicy=ERROR`: todo campo destino no mapeado debe marcarse con
  `@Mapping(target = "...", ignore = true)` o la compilación falla.

**Arquitectura:** monolito simple en capas (controller → service → repository).
No usar microservicios ni monolito modular — no se justifica para este proyecto
(1-2 usuarios, bajo volumen).

**Alcance:** no agregar funcionalidad fuera de lo definido en `DESIGN.md` (roles
granulares, multi-tenancy, colas de mensajes, cache distribuido, etc.) salvo que se
pida explícitamente.

## Control de versiones (Git)

**No ejecutes `git commit` ni `git push` por tu cuenta.** El commit lo hago yo
manualmente después de revisar los cambios.

En su lugar, al terminar una tarea o fase, **sugiere el mensaje de commit** siguiendo
el estándar [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope opcional>): <descripción corta en minúsculas, sin punto final>

[cuerpo opcional explicando el porqué, no el qué]
```

Tipos permitidos: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `build`.

Ejemplos:
```
feat(auth): add JWT login endpoint
feat(clients): implement client CRUD endpoints
fix(technical-reports): correct report_number sequence on concurrent creation
docs: update DESIGN.md with confirmed tech stack
```

Si los cambios de una tarea tocan varios módulos de forma no relacionada, sugiere
mensajes de commit separados por módulo en vez de uno solo genérico.

## Cómo trabajar en este proyecto

- Implementa **una fase de `DESIGN.md` a la vez**, en el orden en que están listadas.
  No adelantes fases posteriores sin que se confirme que la actual funciona.
- Antes de escribir código nuevo, revisa si ya existen entidades/servicios similares
  en el proyecto para mantener el mismo estilo.
- Al terminar una fase, resume brevemente qué se implementó y qué falta probar
  manualmente (ej. "prueba el login con estas credenciales de seed").

## Comandos útiles

```bash
docker compose up -d        # PostgreSQL local (compose.yaml)
JWT_SECRET=<mín. 32 caracteres> ADMIN_EMAIL=<email> ADMIN_PASSWORD=<clave> \
  ./mvnw spring-boot:run     # levantar la app localmente
./mvnw test                 # correr tests (requiere Docker activo por Testcontainers)
```

`JWT_SECRET` es obligatoria (sin ella la app no arranca). `ADMIN_EMAIL` / `ADMIN_PASSWORD`
solo se usan la primera vez, cuando la tabla `users` está vacía. Para empezar de cero:
`docker compose down -v`.

Las migraciones Flyway se aplican automáticamente al arrancar la app (no se usa el
`flyway-maven-plugin`).
