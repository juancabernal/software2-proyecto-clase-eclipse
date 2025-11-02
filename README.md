# UCO Challenge – Flujo de usuarios

Este documento resume el flujo actualizado para registrar y listar usuarios a través del
frontend (React), el API Gateway (Spring Cloud Gateway) y el backend UCOChallenge.

## Variables de entorno

El frontend Auth0 SPA requiere las siguientes variables (archivo `.env`):

```bash
VITE_API_SERVER_URL=http://localhost:8085
VITE_AUTH0_AUDIENCE=https://uco-challenge-api
```

## Registro de usuarios

Cadena de invocación:

```
React (POST /api/admin/users)
→ API Gateway (POST /api/admin/users)
→ Backend UCOChallenge (POST /uco-challenge/api/v1/users)
→ Persistencia PostgreSQL
```

### Payload esperado

```json
{
  "idType": "<uuid>",
  "idNumber": "1002003000",
  "firstName": "Juan",
  "secondName": "Pablo",
  "firstSurname": "Gómez",
  "secondSurname": "Rojas",
  "homeCity": "<uuid>",
  "email": "juan.gomez@example.com",
  "mobileNumber": "3120000000"
}
```

Alias aceptados por compatibilidad (JSON en español):

| Inglés          | Alias español             |
| --------------- | ------------------------- |
| `idType`        | `tipoIdentificacion`      |
| `idNumber`      | `numeroIdentificacion`    |
| `firstName`     | `primerNombre`            |
| `secondName`    | `segundoNombre`           |
| `firstSurname`  | `primerApellido`          |
| `secondSurname` | `segundoApellido`         |
| `homeCity`      | `ciudad`                  |
| `email`         | `correo`                  |
| `mobileNumber`  | `telefono`                |

Todos los identificadores (`idType`, `homeCity`) deben ser UUID válidos.

### Respuesta de éxito

```json
{
  "userMessage": "Usuario registrado exitosamente.",
  "data": {
    "userId": "<uuid>",
    "fullName": "Juan Pablo Gómez Rojas",
    "email": "juan.gomez@example.com"
  }
}
```

## Listado paginado

React consume `GET /api/admin/users?page=<n>&size=<m>` y recibe:

```json
{
  "userMessage": "Usuarios obtenidos exitosamente.",
  "data": {
    "items": [
      {
        "userId": "<uuid>",
        "idType": "CÉDULA",
        "idNumber": "1002003000",
        "fullName": "Juan Gómez",
        "email": "juan@example.com",
        "mobileNumber": "3120000000",
        "emailConfirmed": false,
        "mobileNumberConfirmed": false
      }
    ],
    "page": 1,
    "size": 10,
    "totalItems": 42,
    "totalPages": 5
  }
}
```

## Catálogos

El frontend carga selectores para tipo de identificación y ciudades mediante:

* `GET /api/admin/users/departments`
* `GET /api/admin/users/cities`
* `GET /api/admin/users/id-types`

Estos endpoints exponen directamente el contenido de las tablas (`Departamento`,
`Ciudad`, `TipoIdentificacion`) sin envolver la respuesta. Cada objeto incluye al
menos `id` y `name`, que el frontend utiliza para poblar los `<select>`.

## Pruebas manuales (cURL)

Reemplaza `<JWT_ADMIN>` con un token válido.

```bash
# Backend directo
curl -X POST "http://localhost:8081/uco-challenge/api/v1/users" \
  -H "Authorization: Bearer <JWT_ADMIN>" \
  -H "Content-Type: application/json" \
  -d '{
    "idType": "<uuid>",
    "idNumber": "1002003000",
    "firstName": "Juan",
    "firstSurname": "Gómez",
    "homeCity": "<uuid>",
    "email": "juan.gomez@example.com",
    "mobileNumber": "3120000000"
  }'

# A través del gateway
curl -X POST "http://localhost:8085/api/admin/users" \
  -H "Authorization: Bearer <JWT_ADMIN>" \
  -H "Content-Type: application/json" \
  -d '{ ...payload de arriba... }'

# Listado paginado
curl -X GET "http://localhost:8085/api/admin/users?page=1&size=10" \
  -H "Authorization: Bearer <JWT_ADMIN>"

# Catálogos
curl -X GET "http://localhost:8085/api/admin/users/departments" \
  -H "Authorization: Bearer <JWT_ADMIN>"

curl -X GET "http://localhost:8085/api/admin/users/cities" \
  -H "Authorization: Bearer <JWT_ADMIN>"

curl -X GET "http://localhost:8085/api/admin/users/id-types" \
  -H "Authorization: Bearer <JWT_ADMIN>"
```

Si el correo, número de identificación o teléfono ya existen, el backend responde con
HTTP 400 y un mensaje descriptivo. Si `idType` u `homeCity` no son UUID válidos el
backend también responde 400 con un mensaje indicando el campo inválido.
