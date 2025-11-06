import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from "axios";
import { User } from "../types/users";

export type ApiSuccessResponse<T> = { userMessage: string; data: T; }; // ✅ FIX: Preserve shared API response typing
export type Page<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number; }; // ✅ FIX: Maintain pagination typing
export type RegisterUserResponse = { userId: string; fullName: string; email: string; }; // ✅ FIX: Keep register response contract
export type CatalogItem = { id: string; name: string; }; // ✅ FIX: Preserve catalog item typing
type ConfirmationResponse = { // ✅ FIX: Extend confirmation payload with verification identifiers
  remainingSeconds: number; // ✅ FIX: Keep countdown seconds for UI timers
  tokenId?: string; // ✅ FIX: Surface legacy token identifier for compatibility
  verificationId?: string; // ✅ FIX: Expose public verification identifier for link validation
};
export type VerificationAttemptResponse = {
  success: boolean;
  expired: boolean;
  attemptsRemaining: number;
  contactConfirmed: boolean;
  allContactsConfirmed: boolean;
  message: string;
  verificationId?: string; // ✅ FIX: Capture verification identifier returned by backend
};


const DEFAULT_FAILURE_MESSAGE = "No fue posible solicitar la validación.";

// --- Helper seguro para POST que devuelve TTL
const postAndReturnTTL = async (
  api: AxiosInstance,
  endpoint: string,
  failureMessage: string
): Promise<ConfirmationResponse> => {
  console.log(`[api.call] postAndReturnTTL -> POST ${endpoint}`);
  try {
    // registramos lo que enviamos (no hay body, sólo endpoint)
    console.log(`[api.call.payload] postAndReturnTTL`, { endpoint });
  } catch (e) { /* noop */ }
  const res = await api.post(endpoint, undefined, { validateStatus: () => true });
  console.log(`[api.result] postAndReturnTTL <- ${endpoint} status=${res.status}`, { responseData: res.data, headers: res.headers });
  if (res.status >= 200 && res.status < 300) {
    const data = (res.data as any)?.data ?? res.data;
    const seconds = Number(data?.remainingSeconds ?? 0);
    const rawTokenId = data?.tokenId ?? null; // ✅ FIX: Capture token identifier from backend response
    const rawVerificationId = data?.verificationId ?? null; // ✅ FIX: Capture verification identifier when provided
    const sanitizedTokenId = typeof rawTokenId === "string" && rawTokenId ? rawTokenId : undefined; // ✅ FIX: Normalize token identifier value
    const sanitizedVerificationId = typeof rawVerificationId === "string" && rawVerificationId
      ? rawVerificationId
      : sanitizedTokenId; // ✅ FIX: Fallback to token identifier when verification id missing
    return {
      remainingSeconds: Number.isFinite(seconds) ? seconds : 0,
      tokenId: sanitizedTokenId,
      verificationId: sanitizedVerificationId,
    }; // ✅ FIX: Return identifiers alongside TTL for frontend storage
  }
  const msg =
    (res.data && (res.data.message || res.data.error)) || failureMessage || DEFAULT_FAILURE_MESSAGE;
  const error: any = new Error(msg);
  error.response = res;
  throw error;
};
const postVerificationCode = async (
  api: AxiosInstance,
  endpoint: string,
  token: string,
  code: string,
  failureMessage: string
): Promise<VerificationAttemptResponse> => { // ✅ FIX: Submit verification code alongside token identifier
  // Añadido logging para auditar petición de verificación
  const verifiedAt = new Date().toISOString();
  try {
    console.log(`[api.call] postVerificationCode -> POST ${endpoint}`, { payload: { code, token, verifiedAt } });
  } catch (e) { /* noop */ }
  const res = await api.post(
    endpoint,
    { code, token, verifiedAt },
    { validateStatus: () => true }
  );
  try {
    console.log(`[api.result] postVerificationCode <- ${endpoint} status=${res.status}`, { responseData: res.data, headers: res.headers });
  } catch (e) { /* noop */ }
  if (res.status >= 200 && res.status < 300) {
    const data = (res.data as any)?.data ?? res.data;
    const userMessage = typeof (res.data as any)?.userMessage === "string"
      ? String((res.data as any).userMessage)
      : "";
    const normalizedSuccess = typeof data?.success === "boolean"
      ? Boolean(data.success)
      : true;
    const normalizedMessage = String(data?.message ?? userMessage ?? "");
    return {
      success: normalizedSuccess,
      expired: Boolean(data?.expired),
      attemptsRemaining: Number(data?.attemptsRemaining ?? 0),
      contactConfirmed: Boolean(data?.contactConfirmed),
      allContactsConfirmed: Boolean(data?.allContactsConfirmed),
      message: normalizedMessage || (normalizedSuccess ? userMessage || "" : failureMessage) || failureMessage,
      verificationId: typeof data?.verificationId === "string" && data?.verificationId
        ? data.verificationId
        : (typeof data?.tokenId === "string" && data?.tokenId ? data.tokenId : undefined),
    };
  }
  const msg =
    (res.data && (res.data.message || res.data.error)) || failureMessage || DEFAULT_FAILURE_MESSAGE;
  const error: any = new Error(msg);
  error.response = res;
  throw error;
};


// --- Single-flight para token (evita múltiples getToken simultáneos)
const makeTokenGetter = (rawGetToken: () => Promise<string>) => {
  let inFlight: Promise<string> | null = null;
  return async () => {
    if (!inFlight) inFlight = rawGetToken().finally(() => { inFlight = null; });
    return inFlight;
  };
};

export const makeApi = (baseURL: string, getTokenRaw: () => Promise<string>) => {
  if (import.meta.env.PROD && !/^https:\/\//i.test(baseURL)) {
    throw new Error("Base URL insegura en producción: se requiere HTTPS.");
  }

  const getToken = makeTokenGetter(getTokenRaw);

  const api = axios.create({
    baseURL,
    timeout: Number(import.meta.env.VITE_HTTP_GLOBAL_TIMEOUT_MS ?? 15000), // global default (kept conservative); createUser will use per-request timeout
    maxContentLength: 10 * 1024 * 1024, // 10MB defensivo
    headers: { Accept: "application/json" },
    // withCredentials: false, // actívalo SOLO si tu API usa cookies
  });

  // --- Adjunta Authorization SOLO si el request va a nuestro baseURL/origen
  api.interceptors.request.use(async (config) => {
    const reqUrl = new URL(config.url ?? "", baseURL);
    const base = new URL(baseURL);

    const sameOrigin = (reqUrl.origin === base.origin);
    if (sameOrigin) {
      try {
  const token = await getToken();
        if (token) {
          config.headers = config.headers ?? {};
          (config.headers as any).Authorization = `Bearer ${token}`;
        }
      } catch (error) {
        console.error("Error al obtener el token:", error); // En caso de que falle al obtener el token
      }
    }
    return config;
  });


  // --- Reintento único ante 401 (token expirado) sin bucles
  api.interceptors.response.use(
    (r) => r,
    async (error: AxiosError) => {
      const original = error.config as (AxiosRequestConfig & { _retry?: boolean });
      if (error.response?.status === 401 && !original?._retry) {
        try {
          original._retry = true;
          const token = await getToken(); // fuerza renovación silenciosa si aplica
          original.headers = original.headers ?? {};
          (original.headers as any).Authorization = `Bearer ${token}`;
          return api.request(original);
        } catch {
          // si no se puede renovar, cae al error original
        }
      }
      throw error;
    }
  );

  return {
    // GET /api/admin/users?page=1&size=10...
    async listUsers(params: { page?: number; size?: number; }): Promise<Page<User>> {
      console.log('[api.call] listUsers -> GET /api/admin/users', { params });
      const res = await api.get("/api/admin/users", { params, validateStatus: () => true });
      console.log('[api.result] listUsers <- /api/admin/users', { status: res.status, data: res.data });
      if (res.status !== 200) {
        throw new Error(`Listado usuarios HTTP ${res.status}`);
      }
      const payload = res.data as ApiSuccessResponse<Page<User>>;
      return payload.data;
    },


    // POST /api/admin/users
    // acepta options opcionales: { timeoutMs?, idempotencyKey? }
    async createUser(
      payload: UserCreateInput,
      options?: { timeoutMs?: number; idempotencyKey?: string }
    ): Promise<RegisterUserResponse> {
      const HTTP_TIMEOUT_MS = Number(import.meta.env.VITE_HTTP_TIMEOUT_MS ?? 60000);
      const timeoutMs = options?.timeoutMs ?? HTTP_TIMEOUT_MS;

      const headers: Record<string, string> = {};
      const enableIdempotency = String(import.meta.env.VITE_ENABLE_IDEMPOTENCY ?? "false") === "true";
      const idempotencyKey = options?.idempotencyKey;
      if (enableIdempotency && (idempotencyKey || typeof crypto !== "undefined")) {
        try {
          (headers as any)["Idempotency-Key"] = idempotencyKey || (crypto as any).randomUUID?.() || `${Date.now()}-${Math.random()}`;
        } catch {
          (headers as any)["Idempotency-Key"] = idempotencyKey || `${Date.now()}-${Math.random()}`;
        }
      }

      try {
        // Log detallado de la petición
        console.log('[api.debug] createUser -> Iniciando registro...', {
          endpoint: '/api/admin/users',
          payload,
          timeoutMs,
          hasIdempotencyKey: Boolean(options?.idempotencyKey),
          hasHeaders: Boolean(Object.keys(headers).length)
        });

        // Log de headers (omitiendo sensibles)
        const safeHeaders = { ...headers };
        delete safeHeaders['Authorization'];
        delete safeHeaders['Cookie'];
        console.log('[api.debug] createUser -> Headers:', safeHeaders);

        const res = await api.post(
          "/api/admin/users",
          payload,
          {
            validateStatus: () => true,
            timeout: timeoutMs,
            timeoutErrorMessage: "Tiempo de espera agotado al crear el usuario.",
            headers,
          }
        );

        // Log detallado de la respuesta 
        console.log('[api.debug] createUser <- Respuesta recibida:', {
          status: res.status,
          success: res.status === 201,
          dataPresent: Boolean(res.data),
          contentType: res.headers?.['content-type'],
          responseSize: JSON.stringify(res.data).length
        });

        // Log del cuerpo de la respuesta
        if (res.data) {
          console.log('[api.debug] createUser <- Cuerpo de respuesta:', {
            data: res.data,
            error: res.data.error || res.data.message || null
          });
        }

        if (res.status !== 201) {
          const data = res.data ?? {};
          const msg = data.userMessage || data.technicalMessage || data.message || "No se pudo crear el usuario";
          
          // Log detallado del error
          console.error('[api.error] createUser <- Error al crear usuario:', {
            status: res.status,
            endpoint: '/api/admin/users',
            errorMessage: msg,
            validationErrors: data.errors || [],
            technicalDetails: data.technicalMessage || null,
            rawError: data.error || null
          });

          const error: any = new Error(msg);
          error.response = { status: res.status, data };

          throw error;
        }

        const payloadResponse = res.data as ApiSuccessResponse<RegisterUserResponse>;
        return payloadResponse.data;
      } catch (err: any) {
        // Mapear timeouts de Axios para proveer un mensaje más amigable.
        if (err?.code === 'ECONNABORTED' || /timeout/i.test(String(err?.message || ''))) {
          const timeoutErr: any = new Error("El servidor tardó demasiado en responder. Verificaremos si el usuario fue creado.");
          timeoutErr.code = 'ECONNABORTED';
          timeoutErr.original = err;
          // preserve response if present
          if (err?.response) timeoutErr.response = err.response;
          throw timeoutErr;
        }
        throw err; // rethrow original
      }
    },

    async listIdTypes(): Promise<CatalogItem[]> {
      console.log('[api.call] listIdTypes -> GET /api/admin/catalogs/id-types');
      const res = await api.get("/api/admin/catalogs/id-types", { validateStatus: () => true });
      console.log('[api.result] listIdTypes <- /api/admin/catalogs/id-types', { status: res.status, data: res.data });
      if (res.status !== 200) throw new Error(`Catálogo idType HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async listCities(): Promise<CatalogItem[]> {
      console.log('[api.call] listCities -> GET /api/admin/catalogs/cities');
      const res = await api.get("/api/admin/catalogs/cities", { validateStatus: () => true });
      console.log('[api.result] listCities <- /api/admin/catalogs/cities', { status: res.status, data: res.data });
      if (res.status !== 200) throw new Error(`Catálogo ciudades HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async listDepartments(): Promise<CatalogItem[]> {
      console.log('[api.call] listDepartments -> GET /api/admin/catalogs/departments');
      const res = await api.get("/api/admin/catalogs/departments", { validateStatus: () => true });
      console.log('[api.result] listDepartments <- /api/admin/catalogs/departments', { status: res.status, data: res.data });
      if (res.status !== 200) throw new Error(`Catálogo departamentos HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async listCitiesByDepartment(departmentId: string): Promise<CatalogItem[]> {
      console.log('[api.call] listCitiesByDepartment -> GET /api/admin/catalogs/departments/${departmentId}/cities', { departmentId });
      const res = await api.get(`/api/admin/catalogs/departments/${departmentId}/cities`, { validateStatus: () => true });
      console.log('[api.result] listCitiesByDepartment <- /api/admin/catalogs/departments/${departmentId}/cities', { status: res.status, data: res.data });
      if (res.status !== 200) throw new Error(`Catálogo ciudades por departamento HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    // Helper para buscar localmente si un usuario existe (no rompe contrato público)
    async findUserLocally(params: { email?: string; idNumber?: string }): Promise<User | null> {
      const page0 = 0;
      const size = 50;
      console.log('[api.call] findUserLocally -> GET /api/admin/users', { query: { page: page0, size } });
      const res = await api.get("/api/admin/users", { params: { page: page0, size }, validateStatus: () => true });
      console.log('[api.result] findUserLocally <- /api/admin/users', { status: res.status, data: res.data });
      if (res.status !== 200) {
        // no queremos lanzar aquí para que el caller decida (fallback silencioso)
        try {
          console.warn({ event: "findUserLocally:failed", status: res.status });
        } catch (e) {
          /* noop */
        }
        return null;
      }
      const payload = (res.data as ApiSuccessResponse<Page<User>>)?.data ?? (res.data as Page<User>);
      const items = payload?.items ?? [];
      const found = items.find((u) => {
        if (params.email && u.email && params.email) {
          if (u.email.toLowerCase() === params.email.toLowerCase()) return true;
        }
        if (params.idNumber && u.idNumber && params.idNumber) {
          if (u.idNumber === params.idNumber) return true;
        }
        return false;
      });
      return found ?? null;
    },

    async requestEmailConfirmation(userId: string): Promise<ConfirmationResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) throw new Error("Es necesario proporcionar el identificador del usuario.");

      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirmations/email`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirmations/email`;

      try {
        console.log('[api.call] requestEmailConfirmation', { userId: trimmedId, adminEndpoint, fallbackEndpoint });
        return await postAndReturnTTL(api, adminEndpoint, "No fue posible solicitar la validación del correo electrónico.");
      } catch (error: any) {
        console.log('[api.error] requestEmailConfirmation error', { error: error?.response?.status ?? error?.message ?? error });
        if (error?.response?.status === 404) {
          return await postAndReturnTTL(api, fallbackEndpoint, "No fue posible solicitar la validación del correo electrónico.");
        }
        throw error;
      }
    },

    async requestMobileConfirmation(userId: string): Promise<ConfirmationResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) throw new Error("Es necesario proporcionar el identificador del usuario.");

      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirmations/mobile`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirmations/mobile`;

      try {
        console.log('[api.call] requestMobileConfirmation', { userId: trimmedId, adminEndpoint, fallbackEndpoint });
        return await postAndReturnTTL(api, adminEndpoint, "No fue posible solicitar la validación del teléfono móvil.");
      } catch (error: any) {
        console.log('[api.error] requestMobileConfirmation error', { error: error?.response?.status ?? error?.message ?? error });
        if (error?.response?.status === 404) {
          return await postAndReturnTTL(api, fallbackEndpoint, "No fue posible solicitar la validación del teléfono móvil.");
        }
        throw error;
      }
    },
    async validateEmailConfirmation(userId: string, token: string, code: string): Promise<VerificationAttemptResponse> { // ✅ FIX: Require token identifier for email confirmation
      const trimmedId = userId?.trim();
      if (!trimmedId) {
        throw new Error("Es necesario proporcionar el identificador del usuario.");
      }
      const sanitizedToken = token?.trim(); // ✅ FIX: Normalize token identifier before submission
      if (!sanitizedToken) { // ✅ FIX: Ensure token identifier is present
        throw new Error("El identificador del token es obligatorio para validar el código.");
      }
      const sanitizedCode = code?.trim();
      if (!sanitizedCode) {
        throw new Error("Debes ingresar el código de verificación.");
      }

      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirmations/email/verify`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirmations/email/verify`;

      try {
        console.log('[api.call] validateEmailConfirmation', { userId: trimmedId, token: sanitizedToken, code: sanitizedCode, adminEndpoint, fallbackEndpoint });
        return await postVerificationCode(
          api,
          adminEndpoint,
          sanitizedToken,
          sanitizedCode,
          "No fue posible validar el código del correo electrónico."
        );
      } catch (error: any) {
        console.log('[api.error] validateEmailConfirmation error', { error: error?.response?.status ?? error?.message ?? error });
        if (error?.response?.status === 404) {
          return await postVerificationCode(
            api,
            fallbackEndpoint,
            sanitizedToken,
            sanitizedCode,
            "No fue posible validar el código del correo electrónico."
          );
        }
        throw error;
      }
    },

    async validateMobileConfirmation(userId: string, token: string, code: string): Promise<VerificationAttemptResponse> { // ✅ FIX: Require token identifier for mobile confirmation
      const trimmedId = userId?.trim();
      if (!trimmedId) {
        throw new Error("Es necesario proporcionar el identificador del usuario.");
      }
      const sanitizedToken = token?.trim(); // ✅ FIX: Normalize token identifier before submission
      if (!sanitizedToken) { // ✅ FIX: Ensure token identifier is present for validation
        throw new Error("El identificador del token es obligatorio para validar el código.");
      }
      const sanitizedCode = code?.trim();
      if (!sanitizedCode) {
        throw new Error("Debes ingresar el código de verificación.");
      }

      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirmations/mobile/verify`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirmations/mobile/verify`;

      try {
        console.log('[api.call] validateMobileConfirmation', { userId: trimmedId, token: sanitizedToken, code: sanitizedCode, adminEndpoint, fallbackEndpoint });
        return await postVerificationCode(
          api,
          adminEndpoint,
          sanitizedToken,
          sanitizedCode,
          "No fue posible validar el código del teléfono móvil."
        );
      } catch (error: any) {
        console.log('[api.error] validateMobileConfirmation error', { error: error?.response?.status ?? error?.message ?? error });
        if (error?.response?.status === 404) {
          return await postVerificationCode(
            api,
            fallbackEndpoint,
            sanitizedToken,
            sanitizedCode,
            "No fue posible validar el código del teléfono móvil."
          );
        }
        throw error;
      }
    },

    async verifyUserToken(token: string): Promise<VerificationAttemptResponse> { // ✅ FIX: Expose helper for public verification endpoint
      const sanitizedToken = token?.trim(); // ✅ FIX: Normalize public token identifier from verification link
      if (!sanitizedToken) { // ✅ FIX: Ensure token presence before requesting backend
        throw new Error("El token de verificación es obligatorio.");
      }

      const endpoint = `/api/v1/users/verify`; // ✅ FIX: Target new verification endpoint
  console.log('[api.call] verifyUserToken -> POST /api/v1/users/verify', { token: sanitizedToken });
  const res = await api.post(endpoint, { token: sanitizedToken }, { validateStatus: () => true }); // ✅ FIX: Submit token for backend validation
  console.log('[api.result] verifyUserToken <- /api/v1/users/verify', { status: res.status, data: res.data });
      if (res.status >= 200 && res.status < 300) { // ✅ FIX: Accept success HTTP codes
        const data = (res.data as any)?.data ?? res.data; // ✅ FIX: Support wrapped responses from API
        return {
          success: Boolean(data?.success), // ✅ FIX: Surface backend success status
          expired: Boolean(data?.expired), // ✅ FIX: Communicate expiration state
          attemptsRemaining: Number(data?.attemptsRemaining ?? 0), // ✅ FIX: Maintain attempt counter information
          contactConfirmed: Boolean(data?.contactConfirmed), // ✅ FIX: Signal contact confirmation status
          allContactsConfirmed: Boolean(data?.allContactsConfirmed), // ✅ FIX: Propagate global confirmation state
          message: String(data?.message ?? "") || "", // ✅ FIX: Preserve backend message for UI display
          verificationId: typeof data?.verificationId === "string" && data?.verificationId
            ? data.verificationId
            : (typeof data?.tokenId === "string" && data?.tokenId ? data.tokenId : undefined),
        }; // ✅ FIX: Return normalized verification response structure
      }
      const msg = (res.data && (res.data.message || res.data.error)) || "No fue posible validar el token."; // ✅ FIX: Provide descriptive fallback message
      const error: any = new Error(msg); // ✅ FIX: Raise error with backend message context
      error.response = res; // ✅ FIX: Attach response details for upstream handling
      throw error; // ✅ FIX: Propagate failure to calling component
    },


  };
};


// Tipos
export type UserCreateInput = {
  idType: string;
  idNumber: string;
  firstName: string;
  secondName?: string;
  firstSurname: string;
  secondSurname?: string;
  homeCity: string;
  email: string;
  mobileNumber?: string;
};
