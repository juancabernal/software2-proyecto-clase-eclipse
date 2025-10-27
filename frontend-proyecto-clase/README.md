# Frontend proyecto clase

Este frontend usa React + Vite y se conecta contra Auth0 para autenticar administradores y consumir el backend `auth0-backend`.

## Configuración de entorno

1. Copia el archivo `.env.example` a `.env` dentro de `frontend-proyecto-clase/` y ajusta los valores según tu tenant de Auth0:

   ```bash
   cd frontend-proyecto-clase
   cp .env.example .env
   ```

2. Variables relevantes:
   - `VITE_AUTH0_DOMAIN` y `VITE_AUTH0_CLIENT_ID`: credenciales de tu aplicación SPA en Auth0.
   - `VITE_AUTH0_AUDIENCE`: identificador (API Identifier) de la API protegida que expone scopes `read` y/o `write`.
   - `VITE_AUTH0_API_SCOPE`: scopes a solicitar en los tokens de acceso (por ejemplo `read write`).
   - `VITE_AUTH0_ADMIN_CLAIM`: claim personalizada donde Auth0 expone los roles (por defecto `https://<tu-dominio>/roles`).
   - `VITE_API_BASE_URL`: URL del backend que recibe las peticiones autenticadas.

3. Los `Allowed Callback URLs`, `Allowed Logout URLs` y `Allowed Web Origins` del tenant deben incluir `http://localhost:5173` (frontend) y `http://localhost:8085/login/oauth2/code/auth0` (backend Spring).

## Puesta en marcha

```bash
cd frontend-proyecto-clase
npm install
npm run dev
```

El backend `auth0-backend` se levanta con Maven:

```bash
cd auth0-backend
./mvnw spring-boot:run
```

## Notas

- El frontend fuerza la redirección de usuarios que no tengan el rol `admin` definido en la metadata de Auth0.
- Los tokens se almacenan en `localstorage` y se revalidan con `getAccessTokenSilently` para adjuntar el `Bearer` en las peticiones al backend.
