# Instalación local en el PC de un operario

Guía para instalar el sistema completo (API + base de datos) en una PC o laptop con
Windows, para usarlo sin conexión a Azure. La instalación la hace una persona técnica; el
uso diario no requiere conocimientos técnicos.

> **Importante:** el sistema es una API. Mientras no exista un frontend, el operario la
> usa desde **Swagger UI** en el navegador (`http://localhost:8080/swagger-ui.html`), que
> permite ejecutar todas las operaciones con formularios.

## 1. Opción recomendada: Docker Desktop

Todo corre en dos contenedores (la API y PostgreSQL), sin instalar Java ni PostgreSQL en
Windows. Los datos se guardan en un volumen de Docker que sobrevive a reinicios y
actualizaciones.

### Requisitos

| Requisito | Mínimo |
|---|---|
| Sistema operativo | Windows 10 22H2 o Windows 11 (64 bits) |
| Memoria | 8 GB de RAM |
| Disco | 10 GB libres |
| BIOS | Virtualización activada (VT-x / AMD-V) |
| Permisos | Administrador para instalar Docker Desktop |
| Red | Internet solo durante la instalación y las actualizaciones |

**Licencia de Docker Desktop:** es gratuita para empresas con menos de 250 empleados **y**
menos de USD 10 millones de ingresos anuales. Si Scontrol supera cualquiera de los dos
umbrales, requiere una suscripción de pago; en ese caso usar la opción B (sección 7).

### Paso 1 — Instalar Docker Desktop

1. Descargar Docker Desktop para Windows desde <https://www.docker.com/products/docker-desktop/>.
2. Instalarlo con la opción **Use WSL 2** (recomendada) y reiniciar si lo pide.
3. Abrir Docker Desktop y, en *Settings → General*, activar **Start Docker Desktop when
   you sign in to your computer**. Así el sistema arranca solo al encender el PC.
4. Comprobar en PowerShell:

   ```powershell
   docker version
   docker compose version
   ```

### Paso 2 — Copiar la aplicación

Copiar la carpeta del proyecto (entregada como `.zip` o clonada desde el repositorio) en
una ruta sin espacios, por ejemplo:

```
C:\scontrol\technical-reports
```

### Paso 3 — Configurar las claves

En esa carpeta, copiar `.env.example` como `.env` y editarlo con el Bloc de notas:

```powershell
cd C:\scontrol\technical-reports
copy .env.example .env
notepad .env
```

| Variable | Qué poner |
|---|---|
| `DB_USERNAME` | Usuario de la base de datos (puede quedar `technical_reports`) |
| `DB_PASSWORD` | Clave larga y aleatoria para la base de datos |
| `JWT_SECRET` | Clave aleatoria de **32 caracteres o más** |
| `ADMIN_EMAIL` | Email con el que el operario iniciará sesión |
| `ADMIN_PASSWORD` | Clave inicial del operario |

Para generar claves aleatorias en PowerShell:

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
```

Guardar una copia de `.env` en un lugar seguro: sin `DB_PASSWORD` no se puede acceder a los
datos. `DB_PASSWORD` y `DB_USERNAME` **no deben cambiarse** después de la primera
instalación, porque la base de datos ya se creó con esos valores.

### Paso 4 — Instalar y arrancar

```powershell
cd C:\scontrol\technical-reports
docker compose -f compose.local.yaml up -d --build
```

La primera vez tarda varios minutos: descarga las imágenes y compila la aplicación. Al
terminar, los contenedores quedan configurados para arrancar solos con Docker Desktop.

### Paso 5 — Verificar

1. Abrir `http://localhost:8080/swagger-ui.html` en el navegador.
2. En **Auth → POST /api/auth/login**, pulsar *Try it out* e iniciar sesión con
   `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
3. Copiar el `accessToken` de la respuesta, pulsar **Authorize** (arriba a la derecha) y
   pegarlo.
4. Probar `GET /api/catalogs?type=STATUS`: debe devolver Operativo, Inoperativo y En Obs.

El sistema solo es accesible desde el propio PC (`127.0.0.1`). Ver la sección 6 si se
necesita acceso desde otros equipos de la red.

## 2. Uso diario

- El sistema arranca solo al iniciar sesión en Windows (Docker Desktop + `restart:
  unless-stopped`).
- El operario abre `http://localhost:8080/swagger-ui.html`, inicia sesión y autoriza con el
  token. El token dura 10 horas.
- Para descargar un PDF: **GET /api/technical-reports/{id}/document** → *Download file*.

Ver [`API.md`](API.md) para el detalle de cada operación y el flujo típico.

## 3. Comandos de mantenimiento

Ejecutar siempre desde `C:\scontrol\technical-reports`.

| Acción | Comando |
|---|---|
| Ver estado | `docker compose -f compose.local.yaml ps` |
| Ver logs de la API | `docker compose -f compose.local.yaml logs -f app` |
| Detener | `docker compose -f compose.local.yaml stop` |
| Arrancar | `docker compose -f compose.local.yaml start` |
| Actualizar a una nueva versión | Reemplazar los archivos de la carpeta (conservando `.env`) y ejecutar `docker compose -f compose.local.yaml up -d --build` |

Al actualizar, las migraciones de base de datos se aplican solas y los datos se conservan.

## 4. Copias de seguridad

Los datos viven en el volumen de Docker `technical-reports_db-data`. Se recomienda una
copia semanal guardada **fuera del PC** (USB, OneDrive o servidor).

**Crear copia:**

```powershell
cd C:\scontrol\technical-reports
mkdir backups -ErrorAction SilentlyContinue
docker compose -f compose.local.yaml exec db pg_dump -U technical_reports -Fc technical_reports -f /tmp/backup.dump
docker compose -f compose.local.yaml cp db:/tmp/backup.dump ".\backups\backup-$(Get-Date -Format yyyy-MM-dd).dump"
```

Si se cambió `DB_USERNAME`, usar ese valor en lugar de `technical_reports` después de `-U`.

**Restaurar una copia** (reemplaza los datos actuales):

```powershell
docker compose -f compose.local.yaml stop app
docker compose -f compose.local.yaml cp ".\backups\backup-2026-09-29.dump" db:/tmp/restore.dump
docker compose -f compose.local.yaml exec db pg_restore -U technical_reports -d technical_reports --clean --if-exists /tmp/restore.dump
docker compose -f compose.local.yaml start app
```

## 5. Problemas frecuentes

| Síntoma | Solución |
|---|---|
| `http://localhost:8080` no responde | Verificar que Docker Desktop esté abierto y ejecutar `docker compose -f compose.local.yaml ps`. Revisar `logs app` |
| Error `definir JWT_SECRET en .env` al arrancar | Falta el archivo `.env` o una variable dentro (paso 3) |
| La API no arranca y el log dice que `JWT_SECRET` debe tener al menos 32 bytes | Alargar `JWT_SECRET` y ejecutar `up -d` de nuevo |
| Puerto 8080 ocupado | Cerrar el programa que lo usa o cambiar `127.0.0.1:8080:8080` por `127.0.0.1:8081:8080` en `compose.local.yaml` |
| Login correcto pero `401` en el resto | El token expiró (10 h) o no se pulsó *Authorize* |
| El operario olvidó su clave | Ver abajo |

**Restablecer la clave de un usuario:** no hay pantalla para ello; se hace desde la base
de datos (reemplazar el email y la nueva clave):

```powershell
docker compose -f compose.local.yaml exec db psql -U technical_reports -d technical_reports -c "CREATE EXTENSION IF NOT EXISTS pgcrypto; UPDATE users SET password_hash = crypt('NuevaClave123', gen_salt('bf', 10)) WHERE email = 'operario@scontrol.pe';"
```

Cambiar `ADMIN_PASSWORD` en `.env` **no** cambia la clave: solo se usa al crear el primer
usuario con la base vacía.

## 6. Acceso desde otros equipos de la red (opcional)

Por seguridad, la API solo escucha en `127.0.0.1`. Para abrirla a la red local:

1. En `compose.local.yaml`, cambiar `"127.0.0.1:8080:8080"` por `"8080:8080"`.
2. Permitir el puerto 8080 en el Firewall de Windows solo para la red privada.
3. Ejecutar `docker compose -f compose.local.yaml up -d`.

El tráfico viaja sin cifrar (HTTP) dentro de la red; no exponer el puerto a Internet.

## 7. Opción B: instalación sin Docker

Para PCs donde no se puede usar Docker Desktop:

1. Instalar **Java 21** (Eclipse Temurin JRE 21, instalador `.msi`, marcando *Set JAVA_HOME*).
2. Instalar **PostgreSQL 17** con el instalador oficial para Windows, y crear la base
   `technical_reports` y un usuario con permisos sobre ella.
3. Copiar el JAR de la aplicación (`technical-reports-<versión>.jar`, generado con
   `./mvnw package -DskipTests` en un equipo de desarrollo) en `C:\scontrol\technical-reports`.
4. Crear `C:\scontrol\technical-reports\start.bat`:

   ```bat
   @echo off
   set DB_URL=jdbc:postgresql://localhost:5432/technical_reports
   set DB_USERNAME=technical_reports
   set DB_PASSWORD=<clave de la base>
   set JWT_SECRET=<clave de 32 o más caracteres>
   set ADMIN_EMAIL=operario@scontrol.pe
   set ADMIN_PASSWORD=<clave inicial>
   java -Duser.timezone=America/Lima -jar technical-reports-<versión>.jar
   ```

5. Para que arranque con Windows, registrarlo como servicio con una herramienta como
   [WinSW](https://github.com/winsw/winsw), o crear una tarea programada al iniciar sesión.

Esta opción requiere mantener Java y PostgreSQL actualizados a mano, y el `start.bat`
contiene claves en texto plano: restringir los permisos de la carpeta al usuario del
operario. Las copias de seguridad se hacen con `pg_dump` desde la instalación de PostgreSQL.
