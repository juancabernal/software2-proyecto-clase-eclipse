import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from "axios";
import { User } from "../types/users";

export type ApiSuccessResponse<T> = { userMessage: string; data: T; }; // ✅ FIX: Preserve shared API response typing
export type Page<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number; }; // ✅ FIX: Maintain pagination typing
export type RegisterUserResponse = { userId: string; fullName: string; email: string; }; // ✅ FIX: Keep register response contract
export type CatalogItem = { id: string; name: string; }; // ✅ FIX: Preserve catalog item typing
type ConfirmationResponse = { // ✅ FIX: Extend confirmation payload with verification identifiers
  remainingSeconds: number; // ✅ FIX: Keep countdown seconds for UI timers
  contact?: string; // ✅ FIX: Surface target contact for contextual messaging
  channel?: string; // ✅ FIX: Keep channel information returned by backend
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

export type ConfirmVerificationResponse = {
  confirmed: boolean;
  message?: string;
};


const DEFAULT_FAILURE_MESSAGE = "No fue posible solicitar la validación.";

const EMAIL_VERIFICATION_STORAGE_KEY = "api.admin.users.email.verificationId";
let emailVerificationIdCache: string | undefined;

const persistEmailVerificationId = (value?: string | null) => {
  const sanitizedValue = typeof value === "string" && value.trim().length > 0 ? value.trim() : undefined;
  emailVerificationIdCache = sanitizedValue;
  try {
    if (typeof window !== "undefined" && window?.localStorage) {
      if (sanitizedValue) {
        window.localStorage.setItem(EMAIL_VERIFICATION_STORAGE_KEY, sanitizedValue);
      } else {
        window.localStorage.removeItem(EMAIL_VERIFICATION_STORAGE_KEY);
      }
    }
  } catch (storageError) {
    console.warn("[api.warn] persistEmailVerificationId -> storage error", storageError);
  }
};

const readEmailVerificationId = (): string | undefined => {
  if (emailVerificationIdCache && emailVerificationIdCache.trim().length > 0) {
    return emailVerificationIdCache;
  }

  try {
    if (typeof window !== "undefined" && window?.localStorage) {
      const stored = window.localStorage.getItem(EMAIL_VERIFICATION_STORAGE_KEY);
      if (stored && stored.trim().length > 0) {
        emailVerificationIdCache = stored.trim();
        return emailVerificationIdCache;
      }
    }
  } catch (storageError) {
    console.warn("[api.warn] readEmailVerificationId -> storage error", storageError);
  }

  return undefined;
};

// --- Helper seguro para POST que devuelve TTL
const postAndReturnTTL = async (
  api: AxiosInstance,
  endpoint: string,
  failureMessage: string,
  params?: Record<string, string | null | undefined>
): Promise<ConfirmationResponse> => {
  console.log(`[api.call] postAndReturnTTL -> POST ${endpoint}`);
  try {
    // registramos lo que enviamos (no hay body, sólo endpoint)
    console.log(`[api.call.payload] postAndReturnTTL`, { endpoint, params });
  } catch (e) { /* noop */ }
  const res = await api.post(endpoint, undefined, { validateStatus: () => true, params });
  console.log(`[api.result] postAndReturnTTL <- ${endpoint} status=${res.status}`, {
    responseData: res.data,
    // Separate userMessage and data for clearer logging
    message: res.data?.userMessage,
    data: {
      verificationId: res.data?.data?.verificationId,
      contact: res.data?.data?.contact,
      channel: res.data?.data?.channel,
      remainingSeconds: res.data?.data?.remainingSeconds
    },
    headers: res.headers
  });
  try {
    // Log the raw axios response for deep inspection when debugging missing verification identifiers
    console.log('[api.result] postAndReturnTTL RAW res', {
      status: res.status,
      statusText: res.statusText,
      url: res.config?.url,
      fullData: res.data,
      expectedFormat: {
        userMessage: "string",
        data: {
          verificationId: "UUID string",
          contact: "email or phone",
          channel: "EMAIL or MOBILE",
          remainingSeconds: "number"
        }
      },
      headers: res.headers,
      request: res.request && (res.request.responseURL || res.request.path || res.request._header || res.request),
    });
  } catch (e) { /* noop */ }
  if (res.status >= 200 && res.status < 300) {
    const data = (() => {
      // Detectar si la respuesta viene envuelta o plana
      if (res?.data?.data && typeof res.data.data === "object") {
        return res.data.data;
      }
      if (res?.data && typeof res.data === "object") {
        return res.data;
      }
      return {};
    })();
    const seconds = Number(data?.remainingSeconds ?? 0);

    // Log extracted values vs expected format
    try {
      console.log('[api.debug] postAndReturnTTL parsed fields', {
        expectedFields: {
          verificationId: 'UUID string from data.verificationId',
          contact: 'email/phone from data.contact',
          channel: 'EMAIL/MOBILE from data.channel',
          remainingSeconds: 'number from data.remainingSeconds'
        },
        actualFields: {
          verificationId: data?.verificationId,
          contact: data?.contact,
          channel: data?.channel,
          remainingSeconds: data?.remainingSeconds,
          // Include legacy fields in case they're used
          tokenId: data?.tokenId,
          // Log all fields to see what's actually present
          allDataFields: Object.keys(data || {})
        }
      });
    } catch (e) { /* noop */ }

    const rawTokenId = data?.tokenId ?? null;
    const rawVerificationId = data?.verificationId ?? null;
    const sanitizedTokenId = typeof rawTokenId === "string" && rawTokenId ? rawTokenId : undefined; // ✅ FIX: Normalize token identifier value
    const sanitizedVerificationId = typeof rawVerificationId === "string" && rawVerificationId
      ? rawVerificationId
      : sanitizedTokenId; // ✅ FIX: Fallback to token identifier when verification id missing
    return {
      remainingSeconds: Number.isFinite(seconds) ? seconds : 0,
      contact: typeof data?.contact === "string" && data.contact ? data.contact : undefined,
      channel: typeof data?.channel === "string" && data.channel ? data.channel : undefined,
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
      // Enhanced pre-request validation logging
      console.log(`[api.call] postVerificationCode -> PRE-VALIDATION ${endpoint}`, {
        requestMetadata: {
          timestamp: verifiedAt,
          endpoint: endpoint,
          hasToken: Boolean(token),
          hasCode: Boolean(code)
        },
        tokenValidation: {
          length: token?.length,
          isUUID: /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(token),
          value: token,
          expectedFormat: 'UUID v4'
        },
        codeValidation: {
          length: code?.length,
          isValid: /^\d{6}$/.test(code),
          value: code,
          expectedFormat: '6 digits'
        }
      });
    } catch (e) { /* noop */ }

    const res = await api.post(
      endpoint,
      { code, token, verifiedAt },
      { validateStatus: () => true }
    );  try {
    console.log(`[api.result] postVerificationCode <- ${endpoint}`, {
      status: res.status,
      responseData: res.data,
      headers: res.headers,
      validation: {
        success: Boolean(res.data?.data?.success || res.data?.success),
        message: res.data?.data?.message || res.data?.message || res.data?.userMessage,
        verificationId: res.data?.data?.verificationId || res.data?.verificationId,
        tokenId: res.data?.data?.tokenId || res.data?.tokenId,
        // Comparar token enviado vs recibido
        sentToken: token,
        receivedToken: res.data?.data?.verificationId || res.data?.data?.tokenId || res.data?.verificationId || res.data?.tokenId
      }
    });
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
    const adminEndpoint = `/api/admin/users/${encodedId}/send-code`;
    const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/send-code`;
    const params = { channel: "email" };

      const handleResponse = (response: ConfirmationResponse) => {
        if (response?.verificationId) {
          persistEmailVerificationId(response.verificationId);
          console.log('[api.debug] requestEmailConfirmation -> stored verificationId', {
            verificationId: response.verificationId,
            storage: 'localStorage + memory cache'
          });
        } else {
          persistEmailVerificationId(undefined);
          console.warn('[api.warn] requestEmailConfirmation -> verificationId ausente en la respuesta');
        }
        return response;
      };

      try {
        console.log('[api.call] requestEmailConfirmation', { userId: trimmedId, adminEndpoint, fallbackEndpoint });
        const response = await postAndReturnTTL(api, adminEndpoint, "No fue posible solicitar la validación del correo electrónico.", params);
        return handleResponse(response);
      } catch (error: any) {
        console.log('[api.error] requestEmailConfirmation error', { error: error?.response?.status ?? error?.message ?? error });
        try {
          console.log('[api.error] requestEmailConfirmation -> response.data', error?.response?.data);
          console.log('[api.error] requestEmailConfirmation -> response.headers', error?.response?.headers);
        } catch (e) { /* noop */ }
        if (error?.response?.status === 404) {
          const response = await postAndReturnTTL(api, fallbackEndpoint, "No fue posible solicitar la validación del correo electrónico.", params);
          return handleResponse(response);
        }
        throw error;
      }
    },

    async requestMobileConfirmation(userId: string): Promise<ConfirmationResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) throw new Error("Es necesario proporcionar el identificador del usuario.");

    const encodedId = encodeURIComponent(trimmedId);
    const adminEndpoint = `/api/admin/users/${encodedId}/send-code`;
    const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/send-code`;
    const params = { channel: "mobile" };

      try {
        console.log('[api.call] requestMobileConfirmation', { userId: trimmedId, adminEndpoint, fallbackEndpoint });
        return await postAndReturnTTL(api, adminEndpoint, "No fue posible solicitar la validación del teléfono móvil.", params);
      } catch (error: any) {
        console.log('[api.error] requestMobileConfirmation error', { error: error?.response?.status ?? error?.message ?? error });
        try {
          console.log('[api.error] requestMobileConfirmation -> response.data', error?.response?.data);
          console.log('[api.error] requestMobileConfirmation -> response.headers', error?.response?.headers);
        } catch (e) { /* noop */ }
        if (error?.response?.status === 404) {
          return await postAndReturnTTL(api, fallbackEndpoint, "No fue posible solicitar la validación del teléfono móvil.", params);
        }
        throw error;
      }
    },
    async confirmVerificationCode(
      userId: string,
      channel: "email" | "mobile",
      code: string,
      token?: string
    ): Promise<ConfirmVerificationResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) {
        throw new Error("Es necesario proporcionar el identificador del usuario.");
      }

      const sanitizedCode = code?.trim();
      if (!sanitizedCode) {
        throw new Error("Debes ingresar el código de verificación.");
      }
      if (!/^\d{6}$/.test(sanitizedCode)) {
        throw new Error("El código debe tener 6 dígitos.");
      }

      const providedToken = token?.trim();
      const verificationId = providedToken || readEmailVerificationId();
      if (!verificationId) {
        console.error('[api.error] confirmVerificationCode -> missing verificationId', {
          userId: trimmedId,
          channel,
          hint: 'Ejecuta requestEmailConfirmation antes de confirmar el código.'
        });
        throw new Error("No se encontró un identificador de verificación activo. Solicita un nuevo código e inténtalo nuevamente.");
      }

      // Log code validation before making request
      console.log('[api.debug] confirmVerificationCode -> code validation', {
        sanitizedInput: {
          code: sanitizedCode,
          channel,
          userId: trimmedId
        },
        validation: {
          matches6Digits: /^\d{6}$/.test(sanitizedCode),
          length: sanitizedCode.length,
          channel: channel === 'email' ? 'EMAIL' : 'MOBILE',
          expectedFormat: '6 digits numeric code'
        }
      });

      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirm-code`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirm-code`;
      const payload = { channel, code: sanitizedCode, token: verificationId };

      const sendRequest = async (endpoint: string) => {
        // Log detailed request info including verification code and optional token
        console.log('[api.call] confirmVerificationCode -> request details', {
          endpoint,
          request: {
            channel,
            code: sanitizedCode,
            userId: trimmedId,
            tokenSent: verificationId
          },
          validation: {
            codeFormat: {
              value: sanitizedCode,
              length: sanitizedCode.length,
              isValid: /^\d{6}$/.test(sanitizedCode)
            },
            channelInfo: {
              type: channel,
              normalized: channel.toUpperCase()
            }
          }
        });

        const res = await api.post(endpoint, payload, { validateStatus: () => true });

        // Log detailed response including code validation
        console.log('[api.result] confirmVerificationCode -> response analysis', {
          request: {
            sentCode: sanitizedCode,
            endpoint,
            channel
          },
          response: {
            status: res.status,
            data: res.data,
            confirmation: {
              success: Boolean(res.data?.confirmed || res.data?.data?.confirmed),
              message: res.data?.message || res.data?.data?.message
            }
          }
        });

        if (res.status >= 200 && res.status < 300) {
          const responseBody = res.data ?? {};
          const innerData = typeof responseBody.data === "object" ? responseBody.data : {};
          const data = Object.keys(innerData).length > 0 ? innerData : responseBody;
          const confirmed = Boolean(data?.confirmed ?? data?.success);
          const message = typeof data?.message === "string"
            ? data.message
            : (typeof responseBody.userMessage === "string" ? responseBody.userMessage : undefined);
          return { confirmed, message } as ConfirmVerificationResponse;
        }

        const msg = (res.data && (res.data.message || res.data.error))
          || "No fue posible validar el código.";
        const error: any = new Error(msg);
        error.response = res;
        throw error;
      };

      try {
        const response = await sendRequest(adminEndpoint);
        
        // Log code comparison after receiving response
        console.log('[api.debug] confirmVerificationCode -> code comparison', {
          sentCode: {
            value: sanitizedCode,
            channel: channel,
            userId: trimmedId
          },
          response: {
            confirmed: response.confirmed,
            message: response.message,
            success: response.confirmed === true
          },
          codeValidation: {
            originalCode: sanitizedCode,
            isValid: /^\d{6}$/.test(sanitizedCode),
            endpoint: adminEndpoint,
            timestamp: new Date().toISOString()
          }
        });

        return response;
      } catch (error: any) {
        if (error?.response?.status === 404) {
          return await sendRequest(fallbackEndpoint);
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
      if (res.status >= 200 && res.status < 300) {
        // --- Extraer el cuerpo de forma segura, incluso si Axios anida "data" o no ---
        const responseBody = res.data ?? {};
        const innerData = typeof responseBody.data === "object" ? responseBody.data : {};
        const data = Object.keys(innerData).length > 0 ? innerData : responseBody;

        console.log('[api.debug] verifyUserToken parsed payload', {
          raw: res.data,
          extracted: data,
          keys: Object.keys(data || {}),
        });
        console.log('[api.result] verifyUserToken <- /api/v1/users/verify', { status: res.status, data: res.data });
        const rawVerificationId = data?.verificationId ?? null;
        const sanitizedVerificationId =
          typeof rawVerificationId === "string" && rawVerificationId.length > 0
            ? rawVerificationId
            : undefined;

        return {
          success: Boolean(data?.success),
          expired: Boolean(data?.expired),
          attemptsRemaining: Number(data?.attemptsRemaining ?? 0),
          contactConfirmed: Boolean(data?.contactConfirmed),
          allContactsConfirmed: Boolean(data?.allContactsConfirmed),
          message: String(data?.message ?? ""),
          verificationId: sanitizedVerificationId,
        };
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
