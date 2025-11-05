import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from "axios";
import { User } from "../types/users";

export type ApiSuccessResponse<T> = { userMessage: string; data: T; };
export type Page<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number; };
export type RegisterUserResponse = { userId: string; fullName: string; email: string; };
export type CatalogItem = { id: string; name: string; };
type ConfirmationResponse = { remainingSeconds: number };
export type VerificationAttemptResponse = {
  success: boolean;
  expired: boolean;
  attemptsRemaining: number;
  contactConfirmed: boolean;
  allContactsConfirmed: boolean;
  message: string;
export type SearchUsersFilters = {
  idType?: string;        // UUID
  idNumber?: string;
  firstName?: string;
  firstSurname?: string;
  homeCity?: string;      // UUID
  email?: string;
  mobileNumber?: string;
  page?: number;
  size?: number;
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
    return { remainingSeconds: Number.isFinite(seconds) ? seconds : 0 };
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
  code: string,
  failureMessage: string
): Promise<VerificationAttemptResponse> => {
  const res = await api.post(
    endpoint,
    { code },
    { validateStatus: () => true }
  );
  if (res.status >= 200 && res.status < 300) {
    const data = (res.data as any)?.data ?? res.data;
    return {
      success: Boolean(data?.success),
      expired: Boolean(data?.expired),
      attemptsRemaining: Number(data?.attemptsRemaining ?? 0),
      contactConfirmed: Boolean(data?.contactConfirmed),
      allContactsConfirmed: Boolean(data?.allContactsConfirmed),
      message: String(data?.message ?? "") || failureMessage,
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
      } catch {
        // No propagamos detalles del token; dejamos que el request falle si es necesario
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
    async searchUsers(filters: SearchUsersFilters): Promise<Page<User>> {
      const { page = 0, size = 10 } = filters;

      // Construimos params incluyendo todos los filtros que el backend espera.
      // Es importante enviar los filtros de texto (idNumber, firstName, firstSurname, email)
      // para que el endpoint /search reciba las claves correctas y la cache pueda distinguir.
      const params: Record<string, string | number> = { page, size };
      const setIf = (k: keyof SearchUsersFilters) => {
        const v = filters[k];
        if (v !== undefined && v !== null && String(v).trim() !== "") {
          params[k] = String(v).trim();
        }
      };

      // Enviamos todos los filtros esperados por el backend (UUIDs y textos).
      setIf("idType");
      setIf("homeCity");
      setIf("mobileNumber");
      setIf("idNumber");
      setIf("firstName");
      setIf("firstSurname");
      setIf("email");

      // ⬅️ Fallback: si solo hay page/size, no llames /search
      const onlyPaging = Object.keys(params).every((k) => k === "page" || k === "size");
      if (onlyPaging) {
        const resList = await api.get("/api/admin/users", { params, validateStatus: () => true });
        if (resList.status !== 200) {
          const msg = resList.data?.userMessage || resList.data?.technicalMessage || `Listado HTTP ${resList.status}`;
          throw new Error(msg);
        }
        return (resList.data as ApiSuccessResponse<Page<User>>).data;
      }

      const res = await api.get("/api/admin/users/search", {
        params,
        validateStatus: () => true,
      });
      if (res.status !== 200) {
        // Incluimos el body en el mensaje para facilitar debugging local (si está presente).
        const body = res.data ?? null;
        const bodyText = typeof body === "string" ? body : JSON.stringify(body);
        const msg = res.data?.userMessage
          || res.data?.technicalMessage
          || (body ? `Upstream response: ${bodyText}` : `Búsqueda HTTP ${res.status}`);
        const error: any = new Error(msg);
        error.response = res;
        throw error;
      }
      return (res.data as ApiSuccessResponse<Page<User>>).data;
    },
    // POST /api/admin/users
    async createUser(payload: UserCreateInput): Promise<RegisterUserResponse> {
      const res = await api.post("/api/admin/users", payload, { validateStatus: () => true });
      if (res.status !== 201) {
        const msg = (res.data && (res.data.message || res.data.error)) || "No se pudo crear el usuario";
        throw new Error(msg);
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
<<<<<<< HEAD
        async validateEmailConfirmation(userId: string, code: string): Promise<VerificationAttemptResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) {
        throw new Error("Es necesario proporcionar el identificador del usuario.");
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
          sanitizedCode,
          "No fue posible validar el código del correo electrónico."
        );
      } catch (error: any) {
        if (error?.response?.status === 404) {
          return await postVerificationCode(
            api,
            fallbackEndpoint,
            sanitizedCode,
            "No fue posible validar el código del correo electrónico."
          );
        }
        throw error;
      }
    },

    async validateMobileConfirmation(userId: string, code: string): Promise<VerificationAttemptResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) {
        throw new Error("Es necesario proporcionar el identificador del usuario.");
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
          sanitizedCode,
          "No fue posible validar el código del teléfono móvil."
        );
      } catch (error: any) {
        if (error?.response?.status === 404) {
          return await postVerificationCode(
            api,
            fallbackEndpoint,
            sanitizedCode,
            "No fue posible validar el código del teléfono móvil."
          );
        }
        throw error;
      }
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
