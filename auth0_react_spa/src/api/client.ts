import axios, { AxiosInstance } from "axios";
import { User } from "../types/users";

export type ApiSuccessResponse<T> = {
  userMessage: string;
  data: T;
};

export type Page<T> = {
  items: T[];
  page: number;        // 1-based
  size: number;        // page size
  totalItems: number;  // total rowse
  totalPages: number;  // ceil(totalItems/size)
};

export type RegisterUserResponse = {
  userId: string;
  fullName: string;
  email: string;
};

export type CatalogItem = {
  id: string;
  name: string;
};

type ConfirmationResponse = { remainingSeconds: number };
export type VerificationAttemptResponse = {
  success: boolean;
  expired: boolean;
  attemptsRemaining: number;
  contactConfirmed: boolean;
  allContactsConfirmed: boolean;
  message: string;
};

const DEFAULT_FAILURE_MESSAGE = "No fue posible solicitar la validación.";

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


export const makeApi = (baseURL: string, getToken: () => Promise<string>) => {
  const api = axios.create({ baseURL });

  // Bearer token para todas las requests
  api.interceptors.request.use(async (config) => {
    const token = await getToken();
    if (token) {
      config.headers = config.headers ?? {};
      (config.headers as any).Authorization = `Bearer ${token}`;
    }
    return config;
  });

  return {
    // GET /api/admin/users?page=1&size=10&search=&country=...&state=...&city=...
    async listUsers(params: {
      page?: number;
      size?: number;
    }): Promise<Page<User>> {
      const res = await api.get("/api/admin/users", {
        params,
        validateStatus: () => true,
      });
      if (res.status !== 200) {
        throw new Error(`Listado usuarios HTTP ${res.status}`);
      }
      const payload = res.data as ApiSuccessResponse<Page<User>>;
      return payload.data;
    },

    // POST /api/admin/users
    async createUser(payload: UserCreateInput): Promise<RegisterUserResponse> {
      const res = await api.post("/api/admin/users", payload, {
        validateStatus: () => true,
      });
      if (res.status !== 201) {
        const msg = (res.data && (res.data.message || res.data.error)) || "No se pudo crear el usuario";
        throw new Error(msg);
      }
      const payloadResponse = res.data as ApiSuccessResponse<RegisterUserResponse>;
      return payloadResponse.data;
    },

    async listIdTypes(): Promise<CatalogItem[]> {
      const res = await api.get("/api/admin/catalogs/id-types", { validateStatus: () => true });
      if (res.status !== 200) {
        throw new Error(`Catálogo idType HTTP ${res.status}`);
      }
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async listCities(): Promise<CatalogItem[]> {
      const res = await api.get("/api/admin/catalogs/cities", { validateStatus: () => true });
      if (res.status !== 200) {
        throw new Error(`Catálogo ciudades HTTP ${res.status}`);
      }
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
    },

    async requestEmailConfirmation(userId: string): Promise<ConfirmationResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) {
        throw new Error("Es necesario proporcionar el identificador del usuario.");
      }


      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirmations/email`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirmations/email`;

      try {
        return await postAndReturnTTL(
          api,
          adminEndpoint,
          "No fue posible solicitar la validación del correo electrónico."
        );
      } catch (error: any) {
        if (error?.response?.status === 404) {
          return await postAndReturnTTL(
            api,
            fallbackEndpoint,
            "No fue posible solicitar la validación del correo electrónico."
          );
        }
        throw error;
      }
    },

    async requestMobileConfirmation(userId: string): Promise<ConfirmationResponse> {
      const trimmedId = userId?.trim();
      if (!trimmedId) {
        throw new Error("Es necesario proporcionar el identificador del usuario.");
      }


      const encodedId = encodeURIComponent(trimmedId);
      const adminEndpoint = `/api/admin/users/${encodedId}/confirmations/mobile`;
      const fallbackEndpoint = `/uco-challenge/api/v1/users/${encodedId}/confirmations/mobile`;

      try {
        return await postAndReturnTTL(
          api,
          adminEndpoint,
          "No fue posible solicitar la validación del teléfono móvil."
        );
      } catch (error: any) {
        if (error?.response?.status === 404) {
          return await postAndReturnTTL(
            api,
            fallbackEndpoint,
            "No fue posible solicitar la validación del teléfono móvil."
          );
        }
        throw error;
      }
    },
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
  idType: string;        // UUID
  idNumber: string;
  firstName: string;
  secondName?: string;
  firstSurname: string;
  secondSurname?: string;
  homeCity: string;      // UUID
  email: string;
  mobileNumber?: string;
};
