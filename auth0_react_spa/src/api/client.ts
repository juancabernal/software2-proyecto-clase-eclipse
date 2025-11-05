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
  const res = await api.post(endpoint, undefined, { validateStatus: () => true });
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
  // Debug logging removed to avoid exposing sensitive information in the browser console
  const res = await api.post(
    endpoint,
    { code, token },
    { validateStatus: () => true }
  );
  // Response logging removed to avoid exposing sensitive information in the browser console
  if (res.status >= 200 && res.status < 300) {
    const data = (res.data as any)?.data ?? res.data;
    return {
      success: Boolean(data?.success),
      expired: Boolean(data?.expired),
      attemptsRemaining: Number(data?.attemptsRemaining ?? 0),
      contactConfirmed: Boolean(data?.contactConfirmed),
      allContactsConfirmed: Boolean(data?.allContactsConfirmed),
      message: String(data?.message ?? "") || failureMessage,
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
    timeout: 15000, // 15s
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
      const res = await api.get("/api/admin/users", { params, validateStatus: () => true });
      if (res.status !== 200) {
        throw new Error(`Listado usuarios HTTP ${res.status}`);
      }
      const payload = res.data as ApiSuccessResponse<Page<User>>;
      return payload.data;
    },


    // POST /api/admin/users
    async createUser(payload: UserCreateInput): Promise<RegisterUserResponse> {
      const res = await api.post("/api/admin/users", payload, { validateStatus: () => true });
      if (res.status !== 201) {
        // 🔹 Aquí está la diferencia: lanzamos un objeto con 'response' al estilo Axios
        const data = res.data ?? {};
        const msg = data.userMessage || data.technicalMessage || data.message || "No se pudo crear el usuario";
        const error: any = new Error(msg);
        error.response = { data };
        throw error;
      }

      const payloadResponse = res.data as ApiSuccessResponse<RegisterUserResponse>;
      return payloadResponse.data;
    },

    async listIdTypes(): Promise<CatalogItem[]> {
      const res = await api.get("/api/admin/catalogs/id-types", { validateStatus: () => true });
      if (res.status !== 200) throw new Error(`Catálogo idType HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async listCities(): Promise<CatalogItem[]> {
      const res = await api.get("/api/admin/catalogs/cities", { validateStatus: () => true });
      if (res.status !== 200) throw new Error(`Catálogo ciudades HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async listDepartments(): Promise<CatalogItem[]> {
      const res = await api.get("/api/admin/catalogs/departments", { validateStatus: () => true });
      if (res.status !== 200) throw new Error(`Catálogo departamentos HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async listCitiesByDepartment(departmentId: string): Promise<CatalogItem[]> {
      const res = await api.get(`/api/admin/catalogs/departments/${departmentId}/cities`, { validateStatus: () => true });
      if (res.status !== 200) throw new Error(`Catálogo ciudades por departamento HTTP ${res.status}`);
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async requestEmailConfirmation(userId: string): Promise<ConfirmationResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) throw new Error("Es necesario proporcionar el identificador del usuario.");

      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirmations/email`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirmations/email`;

      try {
        return await postAndReturnTTL(api, adminEndpoint, "No fue posible solicitar la validación del correo electrónico.");
      } catch (error: any) {
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
        return await postAndReturnTTL(api, adminEndpoint, "No fue posible solicitar la validación del teléfono móvil.");
      } catch (error: any) {
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
        return await postVerificationCode(
          api,
          adminEndpoint,
          sanitizedToken,
          sanitizedCode,
          "No fue posible validar el código del correo electrónico."
        );
      } catch (error: any) {
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
        return await postVerificationCode(
          api,
          adminEndpoint,
          sanitizedToken,
          sanitizedCode,
          "No fue posible validar el código del teléfono móvil."
        );
      } catch (error: any) {
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
      const res = await api.post(endpoint, { token: sanitizedToken }, { validateStatus: () => true }); // ✅ FIX: Submit token for backend validation
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
