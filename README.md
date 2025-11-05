# 🏛️ UCO Challenge – Arquitectura, Modelos y Flujo de Usuarios

## 📘 Presentación

**UCO Challenge** es un proyecto académico y técnico diseñado como un **reto de arquitectura moderna en la nube**, enfocado en aplicar prácticas avanzadas de desarrollo distribuido con **Spring Boot 3**, **Spring Cloud**, **Azure Key Vault**, **Observabilidad nativa (Prometheus + OpenTelemetry)** y **PostgreSQL**.

El objetivo es demostrar la implementación de un entorno **escalable, seguro y observable**, compuesto por frontend (React), backend distribuido, y servicios auxiliares (catálogos, notificaciones, configuración y trazabilidad).

El proyecto se estructura con un enfoque **Hexagonal / DDD (Domain-Driven Design)**, permitiendo una clara separación entre capas de dominio, aplicación e infraestructura.

### 🔔 Módulo de notificaciones (estructura hexagonal)

- **Puerto de dominio:** `NotificationSenderPort` expone el contrato agnóstico para el envío de notificaciones.
- **Aplicación:** `DuplicateRegistrationNotificationService` orquesta los mensajes de verificación, resuelve destinatarios a través de `NotificationRecipientsProvider` y delega el envío únicamente al puerto.
- **Adaptadores secundarios:**
  - `NotificationApiAdapter` (HTTP) transforma el `NotificationMessage` en el payload externo y reutiliza la configuración `NotificationApiProperties`.
  - `NotificationRecipientsPropertiesAdapter` expone los destinatarios configurados hacia la capa de aplicación.
- **Adaptador primario de pruebas:** `NotificationTestController` ahora vive en `infrastructure/primary/notification` para mantener la dirección de dependencias `domain ← application ← infrastructure`.
- **Pruebas de regresión:** `NotificationApiAdapterTest` valida la serialización del mensaje y la construcción del endpoint antes de invocar el cliente HTTP.

---

## 🧩 Modelo de Clases

📎 **Imagen:** [Ver modelo de clases](https://shorturl.at/xCS8q)

Este modelo define las entidades principales del dominio (`Usuario`, `Ciudad`, `Departamento`, `País`, `TipoIdentificación`), junto con sus relaciones y atributos base.  
Representa la estructura de objetos del sistema y cómo se modelan las reglas de negocio dentro de la aplicación.

---

## 🧮 Modelo MER (Modelo Entidad–Relación)

📎 **Imagen incluida abajo**

Este modelo representa la estructura de datos a nivel de base de datos relacional PostgreSQL.

![Modelo MER](MER-UcoChallenge.jpg)

### Entidades principales

| Entidad | Descripción |
|----------|-------------|
| **País** | Contiene la lista de países disponibles. |
| **Departamento** | Dependiente de país. |
| **Ciudad** | Asociada a un departamento. |
| **TipoIdentificación** | Catálogo de tipos de documento. |
| **Usuario** | Entidad central que relaciona tipo de identificación y ciudad de residencia. |

---

## ☁️ Modelo de Despliegue

### 🧭 Arquitectura de Referencia

📎 **Imagen incluida abajo**

Esta arquitectura representa cómo se despliegan los distintos componentes del ecosistema UCO Challenge en la nube, incluyendo seguridad perimetral, entrega de contenido y monitoreo.

![Arquitectura de Referencia](Captura%20de%20pantalla%202025-11-03%20063715.png)

### 🧱 Arquetipo de Referencia

> _(Espacio reservado para imagen y descripción del arquetipo de referencia del despliegue)_

---

## 📦 Modelo de Paquetes

> _(Espacio reservado para incluir el modelo de paquetes y su descripción correspondiente)_

---

## ⚙️ Modelo de Componentes

📎 **Imagen:** [Ver modelo de componentes](https://shorturl.at/tvLYq)

Este modelo detalla los principales módulos de software dentro del sistema UCO Challenge, incluyendo:
- **Frontend UCOChallenge (React)** – SPA conectada al gateway mediante Auth0.
- **API Gateway (Spring Cloud Gateway)** – Encargado del enrutamiento, seguridad y balanceo.
- **Backend UCOChallenge (Spring Boot)** – Núcleo del dominio y lógica de negocio.
- **Catálogos auxiliares (Message y Parameter Catalog)** – Servicios complementarios para configuración dinámica.
- **Azure Key Vault** – Fuente segura de secretos y configuraciones sensibles.
- **Plataforma de Monitoreo (Prometheus, Grafana, OTLP)** – Observabilidad unificada.
- **Email Notification Gateway** – Servicio externo de notificaciones.

### 📄 Documentación de Componentes

> _(Espacio reservado para agregar la documentación detallada de cada componente: responsabilidades, interfaces y dependencias)_

---

## 🔁 Modelo de Secuencia

📎 **Imagen:** [Ver diagrama de secuencia](https://shorturl.at/kDDke)

Este diagrama describe el flujo completo del caso de uso **Registrar Usuario**, mostrando la interacción entre:
- **Frontend React**
- **API Gateway**
- **Backend UCOChallenge**
- **Repositorios de persistencia y servicios de validación**

---

## ⚙️ Modelo de Actividades

📎 **Imagen:** [Ver diagrama de actividades](https://shorturl.at/BzzNT)

Representa el flujo lógico y de decisión durante el proceso de registro de usuario, incluyendo:
1. Validaciones de datos.
2. Ejecución de reglas de dominio.
3. Persistencia.
4. Respuesta al cliente.

---

## 👥 Flujo de Usuarios

### Variables de entorno

El frontend (SPA Auth0) requiere las siguientes variables:

```bash
VITE_API_SERVER_URL=http://localhost:8085
VITE_AUTH0_AUDIENCE=https://uco-challenge-api


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

* `GET /api/admin/catalogs/id-types`
* `GET /api/admin/catalogs/cities`

Ambos endpoints responden con `ApiSuccessResponse<List<CatalogItemDto>>`, donde cada
`CatalogItemDto` contiene `id` y `name`.

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
curl -X GET "http://localhost:8085/api/admin/catalogs/id-types" \
  -H "Authorization: Bearer <JWT_ADMIN>"

curl -X GET "http://localhost:8085/api/admin/catalogs/cities" \
  -H "Authorization: Bearer <JWT_ADMIN>"
```

Si el correo, número de identificación o teléfono ya existen, el backend responde con
HTTP 400 y un mensaje descriptivo. Si `idType` u `homeCity` no son UUID válidos el
backend también responde 400 con un mensaje indicando el campo inválido.
