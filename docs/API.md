# API REST — Sistema de Informes Técnicos (Scontrol Ingeniería)

Referencia funcional de la API. La especificación formal está en
[`openapi.yaml`](openapi.yaml) (OpenAPI 3.1) y, con la aplicación en marcha, en Swagger UI:
`http://localhost:8080/swagger-ui.html`.

## 1. Convenciones generales

| Tema | Convención |
|---|---|
| URL base | `http://localhost:8080` en local; la URL de Azure en producción |
| Formato | JSON (`Content-Type: application/json`), UTF-8 |
| Fechas y horas | ISO-8601 sin zona horaria, en hora de Perú: `2026-03-02T18:00:00` |
| Fechas (filtros) | `yyyy-MM-dd`, ej. `2026-03-02` |
| Nombres de campos | camelCase en inglés |
| Ids | Números enteros (`Long`) generados por la base de datos |
| Paginación | No hay: los listados devuelven todos los registros (volumen bajo) |
| Textos | Se eliminan los espacios al inicio y al final de todos los campos de texto |

## 2. Autenticación

Todos los endpoints, salvo el login y la documentación, requieren un JWT:

```
Authorization: Bearer <accessToken>
```

### `POST /api/auth/login`

```json
{ "email": "operario@scontrol.pe", "password": "********" }
```

Respuesta `200`:

```json
{ "accessToken": "eyJraWQiOi...", "tokenType": "Bearer", "expiresIn": 36000 }
```

- `expiresIn` está en segundos (10 horas). Al expirar, hay que volver a hacer login.
- El email no distingue mayúsculas de minúsculas.
- Credenciales incorrectas, usuario inexistente o desactivado: `401` con el mismo mensaje
  genérico, para no revelar cuál de los datos falló.
- No hay registro público de usuarios. El primer usuario se crea al arrancar la aplicación
  (ver [`LOCAL_INSTALL.md`](LOCAL_INSTALL.md) o [`AZURE_DEPLOYMENT.md`](AZURE_DEPLOYMENT.md)).

## 3. Errores

Los errores siguen el formato ProblemDetail (RFC 9457):

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "El cliente tiene equipos asociados y no se puede eliminar",
  "instance": "/api/clients/3"
}
```

Los errores de validación incluyen además la lista de campos inválidos:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "La petición tiene campos inválidos",
  "instance": "/api/clients",
  "errors": [
    { "field": "contactEmail", "message": "must be a well-formed email address" },
    { "field": "name", "message": "must not be blank" }
  ]
}
```

| Código | Cuándo |
|---|---|
| `400` | Campos inválidos o faltantes, JSON mal formado, valor de enum desconocido, o referencia inválida en el body (equipo inexistente, opción de catálogo de otro tipo o desactivada) |
| `401` | Sin token, token inválido o expirado, o credenciales incorrectas en el login |
| `404` | El recurso de la URL no existe |
| `409` | Duplicado (documento de cliente, opción de catálogo) o borrado con dependencias |

Los mensajes de `errors[].message` dependen del idioma de la cabecera `Accept-Language`.

## 4. Clientes

Un cliente es una empresa (RUC) o una persona natural (DNI).

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/clients` | Listar, ordenados por nombre | `200` |
| GET | `/api/clients/{id}` | Detalle | `200`, `404` |
| POST | `/api/clients` | Crear | `201` + `Location`, `400`, `409` |
| PUT | `/api/clients/{id}` | Editar (reemplaza todos los campos) | `200`, `400`, `404`, `409` |
| DELETE | `/api/clients/{id}` | Eliminar | `204`, `404`, `409` si tiene equipos |

**Campos**

| Campo | Tipo | Obligatorio | Reglas |
|---|---|---|---|
| `name` | string | Sí | Máx. 200 |
| `documentType` | `RUC` \| `DNI` | No | Va junto con `documentNumber` |
| `documentNumber` | string | No | Solo dígitos: 11 si es RUC, 8 si es DNI. Único entre clientes |
| `contactEmail` | string | No | Email válido, máx. 254 |

`documentType` y `documentNumber` se envían los dos o ninguno.

```json
{
  "name": "FERREYROS",
  "documentType": "RUC",
  "documentNumber": "20100028698",
  "contactEmail": "contacto@ferreyros.com.pe"
}
```

La respuesta devuelve los mismos campos más `id`.

## 5. Equipos

Un equipo es el sistema completo (ej. transmisor + receptor) y siempre pertenece a un
cliente. La lista y la creación cuelgan del cliente; el resto usa la ruta plana.

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/clients/{clientId}/equipment` | Equipos de un cliente, ordenados por modelo | `200`, `404` si el cliente no existe |
| POST | `/api/clients/{clientId}/equipment` | Crear un equipo para el cliente | `201` + `Location: /api/equipment/{id}`, `400`, `404` |
| GET | `/api/equipment/{id}` | Detalle | `200`, `404` |
| PUT | `/api/equipment/{id}` | Editar modelo y N/S | `200`, `400`, `404` |
| DELETE | `/api/equipment/{id}` | Eliminar | `204`, `404`, `409` si tiene informes |

**Campos**

| Campo | Tipo | Obligatorio | Reglas |
|---|---|---|---|
| `model` | string | Sí | Máx. 100, ej. `777 SPECTRUM2` |
| `serialNumber` | string | No | Máx. 255. Texto libre: admite varios N/S, ej. `777 - 19 00160 / 777 - 19 00161` |

El cliente se toma de la ruta y no se puede cambiar después. La respuesta incluye `id`,
`clientId`, `clientName`, `model` y `serialNumber`.

## 6. Catálogos

Opciones configurables que se usan en los informes.

| `type` | Uso en el informe | Ejemplos |
|---|---|---|
| `ACTION` | Acción u operación realizada | Mantenimiento Preventivo, Mantenimiento Correctivo |
| `STATUS` | Estado del equipo al inicio y al término | Operativo, Inoperativo, En Obs. |
| `EVENT_FAILURE` | Evento / falla | Sistema Radiocontrol, Sistema de video |
| `PERSONNEL` | Técnico que firma | Tec. Jorge Antonio Huaman Andia |
| `SUPERVISOR` | Responsable que firma | Ing. Edson Mejia Cielo. |

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/catalogs?type=STATUS` | Opciones de un tipo (activas e inactivas), ordenadas por valor. `type` es obligatorio | `200`, `400` |
| POST | `/api/catalogs` | Crear una opción (nace activa) | `201`, `400`, `409` si ya existe en ese tipo |
| PUT | `/api/catalogs/{id}` | Editar el valor o activar/desactivar | `200`, `400`, `404`, `409` |

```json
// POST
{ "type": "ACTION", "value": "Calibración" }
// PUT
{ "value": "Calibración", "active": false }
// Respuesta
{ "id": 20, "type": "ACTION", "value": "Calibración", "active": false }
```

- No hay DELETE: una opción se desactiva para no romper los informes que ya la usan.
- El tipo de una opción no se puede cambiar.
- Quien consuma la API decide si muestra las opciones inactivas; para crear informes solo
  se aceptan activas.

## 7. Informes técnicos

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/technical-reports` | Listar, más recientes primero | `200` |
| GET | `/api/technical-reports/{id}` | Detalle | `200`, `404` |
| POST | `/api/technical-reports` | Crear; asigna el número automáticamente | `201` + `Location`, `400` |
| PUT | `/api/technical-reports/{id}` | Editar (reemplaza todos los campos) | `200`, `400`, `404` |
| GET | `/api/technical-reports/{id}/document` | Descargar el PDF | `200` (`application/pdf`), `404` |

No hay DELETE: los informes emitidos se conservan.

### Filtros del listado

Todos son opcionales y se combinan entre sí.

| Parámetro | Ejemplo | Filtra por |
|---|---|---|
| `clientId` | `3` | Cliente del equipo |
| `equipmentId` | `7` | Equipo |
| `from` | `2026-03-01` | Fecha de término desde (inclusive) |
| `to` | `2026-03-31` | Fecha de término hasta (inclusive, todo el día) |

### Campos del body (POST y PUT)

| Campo | Tipo | Obligatorio | Reglas |
|---|---|---|---|
| `equipmentId` | number | Sí | Equipo existente |
| `startDatetime` | datetime | No | Si se envía, no puede ser posterior a `endDatetime` |
| `endDatetime` | datetime | Sí | |
| `initialStatusId` | number | Sí | Opción `STATUS` activa |
| `finalStatusId` | number | Sí | Opción `STATUS` activa |
| `eventFailureId` | number | Sí | Opción `EVENT_FAILURE` activa |
| `affectedComponent` | string | No | Máx. 255, ej. `Transmisor Spectrum 2 / receptor FSE777` |
| `actionId` | number | Sí | Opción `ACTION` activa |
| `details` | string | Sí | Texto libre, sin límite práctico |
| `personnelId` | number | Sí | Opción `PERSONNEL` activa |
| `supervisorId` | number | Sí | Opción `SUPERVISOR` activa |

```json
{
  "equipmentId": 7,
  "startDatetime": "2026-03-02T08:00:00",
  "endDatetime": "2026-03-02T18:00:00",
  "initialStatusId": 7,
  "finalStatusId": 6,
  "eventFailureId": 9,
  "affectedComponent": "Transmisor Spectrum 2 / receptor FSE777",
  "actionId": 2,
  "details": "Se continuó con la inspección visual de los componentes...",
  "personnelId": 15,
  "supervisorId": 19
}
```

### Respuesta

Devuelve el informe con el cliente, el equipo y los catálogos ya resueltos, listo para
mostrarse sin más consultas:

```json
{
  "id": 1,
  "reportNumber": "008-0001",
  "clientId": 3,
  "clientName": "FERREYROS",
  "equipmentId": 7,
  "equipmentModel": "777 SPECTRUM2",
  "equipmentSerialNumber": "777 - 19 00160 / 777 - 19 00161",
  "startDatetime": "2026-03-02T08:00:00",
  "endDatetime": "2026-03-02T18:00:00",
  "initialStatus": { "id": 7, "type": "STATUS", "value": "Inoperativo", "active": true },
  "finalStatus": { "id": 6, "type": "STATUS", "value": "Operativo", "active": true },
  "eventFailure": { "id": 9, "type": "EVENT_FAILURE", "value": "Sistema Radiocontrol", "active": true },
  "affectedComponent": "Transmisor Spectrum 2 / receptor FSE777",
  "action": { "id": 2, "type": "ACTION", "value": "Mantenimiento Correctivo", "active": true },
  "details": "Se continuó con la inspección visual de los componentes...",
  "personnel": { "id": 15, "type": "PERSONNEL", "value": "Tec. Jorge Antonio Huaman Andia", "active": true },
  "supervisor": { "id": 19, "type": "SUPERVISOR", "value": "Ing. Edson Mejia Cielo.", "active": true },
  "createdAt": "2026-03-02T18:05:12.345",
  "createdBy": "operario@scontrol.pe"
}
```

### Reglas de negocio

- **Numeración:** formato `<serie>-<correlativo>`, ej. `008-0001`. Correlativo único para
  toda la empresa, sin saltos: una petición rechazada no consume número. Mínimo 4 dígitos;
  después de `008-9999` sigue `008-10000`.
- **Autor:** se toma del usuario del JWT al crear. Al editar no cambian ni el número ni el
  autor.
- **Catálogos:** cada id debe ser una opción existente, del tipo indicado en la tabla y
  activa. Al editar se acepta una opción ya desactivada solo si es la que el informe tenía.
- **Cliente:** es siempre el del equipo; no se envía en el body.

### Documento PDF

`GET /api/technical-reports/{id}/document` devuelve el informe en PDF A4 con el formato de
la plantilla Excel de Scontrol, como adjunto:

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="informe-tecnico-008-0001.pdf"
```

El PDF se genera en cada descarga con los datos actuales del informe; no se almacena.

## 8. Flujo típico

1. `POST /api/auth/login` → guardar `accessToken`.
2. `GET /api/clients` → elegir el cliente, o crearlo con `POST /api/clients`.
3. `GET /api/clients/{clientId}/equipment` → elegir el equipo, o crearlo con `POST`.
4. `GET /api/catalogs?type=...` para cada tipo → opciones de los desplegables (solo activas).
5. `POST /api/technical-reports` → el sistema asigna el número.
6. `GET /api/technical-reports/{id}/document` → descargar el PDF para el cliente.

## 9. Mantener la especificación OpenAPI

`docs/openapi.yaml` se genera desde el código. Un test falla si queda desactualizado; para
regenerarlo tras cambiar un endpoint o un DTO:

```bash
./mvnw test -Dtest=OpenApiDocumentationTests -Dopenapi.export=true
```

En producción, Swagger UI y `/v3/api-docs` se desactivan con `API_DOCS_ENABLED=false`.
