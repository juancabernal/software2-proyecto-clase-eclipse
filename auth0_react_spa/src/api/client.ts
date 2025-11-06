import axios, { AxiosError, AxiosRequestConfig } from "axios";
import { User } from "../types/users";

export type Page<T> = {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
};

export type CatalogItem = { id: string; name: string };

export type RegisterUserResponse = {
  userId: string;
  fullName: string;
  email: string | null;
};

export type ConfirmationRequestResult = {
  success: boolean;
  message: string;
};

export type VerificationAttemptResponse = {
  success: boolean;
  message: string;
  contactConfirmed: boolean;
};

export type UserCreateInput = {
  idTypeId: string;
  idTypeName?: string;
  idNumber: string;
  firstName: string;
  secondName?: string;
  firstSurname: string;
  secondSurname?: string;
  email: string;
  mobile?: string;
  countryId: string;
  departmentId: string;
  cityId: string;
};

const DEFAULT_TIMEOUT = Number(import.meta.env.VITE_HTTP_GLOBAL_TIMEOUT_MS ?? 15000);

const makeTokenGetter = (rawGetToken: () => Promise<string>) => {
  let inFlight: Promise<string> | null = null;
  return async () => {
    if (!inFlight) {
      inFlight = rawGetToken().finally(() => {
        inFlight = null;
      });
    }
    return inFlight;
  };
};

const buildUserFromDto = (dto: any): User => {
  const firstName = String(dto?.firstName ?? "").trim();
  const secondName = String(dto?.secondName ?? "").trim();
  const firstSurname = String(dto?.lastName ?? "").trim();
  const secondSurname = String(dto?.secondSurname ?? "").trim();

  const nameParts = [firstName, secondName, firstSurname, secondSurname].filter(Boolean);

  return {
    userId: String(dto?.id ?? ""),
    idTypeId: dto?.idTypeId ? String(dto.idTypeId) : undefined,
    idNumber: dto?.idNumber ? String(dto.idNumber) : undefined,
    firstName,
    secondName: secondName || undefined,
    firstSurname,
    secondSurname: secondSurname || undefined,
    fullName: nameParts.join(" ") || firstName || firstSurname,
    email: String(dto?.email ?? ""),
    mobileNumber: dto?.mobileNumber ? String(dto.mobileNumber) : undefined,
    emailConfirmed: Boolean(dto?.emailConfirmed),
    mobileNumberConfirmed: Boolean(dto?.mobileNumberConfirmed),
    homeCityId: dto?.homeCityId ? String(dto.homeCityId) : undefined,
  };
};

const computeTotalPages = (totalItems: number, size: number) => {
  if (!Number.isFinite(size) || size <= 0) {
    return 1;
  }
  return Math.max(1, Math.ceil(totalItems / size));
};

export const makeApi = (baseURL: string, getTokenRaw: () => Promise<string>) => {
  if (import.meta.env.PROD && !/^https:\/\//i.test(baseURL)) {
    throw new Error("Base URL insegura en producción: se requiere HTTPS.");
  }

  const getToken = makeTokenGetter(getTokenRaw);

  const api = axios.create({
    baseURL,
    timeout: DEFAULT_TIMEOUT,
    maxContentLength: 10 * 1024 * 1024,
    headers: { Accept: "application/json" },
  });

  api.interceptors.request.use(async (config) => {
    const reqUrl = new URL(config.url ?? "", baseURL);
    const base = new URL(baseURL);

    if (reqUrl.origin === base.origin) {
      try {
        const token = await getToken();
        if (token) {
          config.headers = config.headers ?? {};
          (config.headers as any).Authorization = `Bearer ${token}`;
        }
      } catch (error) {
        console.error("Error al obtener el token:", error);
      }
    }
    return config;
  });

  api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const original = error.config as (AxiosRequestConfig & { _retry?: boolean }) | undefined;
      if (error.response?.status === 401 && original && !original._retry) {
        try {
          original._retry = true;
          const token = await getToken();
          original.headers = original.headers ?? {};
          (original.headers as any).Authorization = `Bearer ${token}`;
          return api.request(original);
        } catch (refreshError) {
          console.error("No fue posible refrescar el token", refreshError);
        }
      }
      throw error;
    }
  );

  const withIdempotencyHeader = async (
    config: AxiosRequestConfig,
    idempotencyKey?: string
  ): Promise<AxiosRequestConfig> => {
    const enableIdempotency = String(import.meta.env.VITE_ENABLE_IDEMPOTENCY ?? "false") === "true";
    if (!enableIdempotency) {
      return config;
    }

    const headers = { ...(config.headers ?? {}) } as Record<string, string>;
    if (idempotencyKey) {
      headers["Idempotency-Key"] = idempotencyKey;
    } else {
      try {
        headers["Idempotency-Key"] = (crypto as any).randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
      } catch {
        headers["Idempotency-Key"] = `${Date.now()}-${Math.random()}`;
      }
    }

    return { ...config, headers };
  };

  const requestConfirmation = async (
    userId: string,
    channel: "email" | "mobile"
  ): Promise<ConfirmationRequestResult> => {
    const sanitizedId = userId.trim();
    if (!sanitizedId) {
      throw new Error("Es necesario proporcionar el identificador del usuario.");
    }

    const response = await api.post(
      `/api/users/${encodeURIComponent(sanitizedId)}/send-code`,
      undefined,
      {
        params: { channel },
        validateStatus: () => true,
      }
    );

    if (response.status >= 200 && response.status < 300) {
      return {
        success: true,
        message:
          channel === "email"
            ? "Se envió la solicitud de validación del correo electrónico."
            : "Se envió la solicitud de validación del teléfono móvil.",
      };
    }

    const message = (response.data as any)?.message ?? "No fue posible solicitar la validación.";
    const error: any = new Error(message);
    error.response = response;
    throw error;
  };

  const verifyCode = async (
    userId: string,
    channel: "email" | "mobile",
    code: string
  ): Promise<VerificationAttemptResponse> => {
    const sanitizedId = userId.trim();
    if (!sanitizedId) {
      throw new Error("Es necesario proporcionar el identificador del usuario.");
    }
    const sanitizedCode = code.trim();
    if (!sanitizedCode) {
      throw new Error("Debes ingresar el código de verificación.");
    }

    const response = await api.post(
      `/api/users/${encodeURIComponent(sanitizedId)}/confirm-code`,
      { channel, code: sanitizedCode },
      { validateStatus: () => true }
    );

    if (response.status >= 200 && response.status < 300) {
      const confirmed = Boolean((response.data as any)?.confirmed);
      return {
        success: confirmed,
        message: confirmed
          ? "Contacto confirmado correctamente."
          : "El código ingresado no es válido.",
        contactConfirmed: confirmed,
      };
    }

    const message = (response.data as any)?.message ?? "No fue posible validar el código.";
    const error: any = new Error(message);
    error.response = response;
    throw error;
  };

  return {
    async listUsers(params: { page?: number; size?: number }): Promise<Page<User>> {
      const requestedPage = Math.max(Number(params?.page ?? 1) - 1, 0);
      const requestedSize = Number(params?.size ?? 10);

      const res = await api.get("/api/users", {
        params: { page: requestedPage, size: requestedSize },
        validateStatus: () => true,
      });

      if (res.status !== 200) {
        throw new Error(`Listado usuarios HTTP ${res.status}`);
      }

      const payload = res.data as {
        users?: any[];
        page?: number;
        size?: number;
        totalElements?: number;
      };

      const items = Array.isArray(payload?.users)
        ? payload.users.map(buildUserFromDto)
        : [];

      const size = Number(payload?.size ?? requestedSize);
      const totalItems = Number(payload?.totalElements ?? items.length);
      const page = Number(payload?.page ?? requestedPage) + 1;

      return {
        items,
        page,
        size,
        totalItems,
        totalPages: computeTotalPages(totalItems, size),
      };
    },

    async createUser(
      payload: UserCreateInput,
      options?: { timeoutMs?: number; idempotencyKey?: string }
    ): Promise<RegisterUserResponse> {
      const timeout = options?.timeoutMs ?? Number(import.meta.env.VITE_HTTP_TIMEOUT_MS ?? 60000);

      const requestConfig = await withIdempotencyHeader(
        {
          validateStatus: () => true,
          timeout,
          timeoutErrorMessage: "Tiempo de espera agotado al crear el usuario.",
        },
        options?.idempotencyKey
      );

      try {
        const res = await api.post("/api/users", payload, requestConfig);

        if (res.status !== 201) {
          const data = res.data ?? {};
          const msg =
            data.userMessage || data.technicalMessage || data.message || "No se pudo crear el usuario";
          const error: any = new Error(msg);
          error.response = { status: res.status, data };
          throw error;
        }

        const data = res.data as {
          id: string;
          firstName?: string;
          middleName?: string;
          lastName?: string;
          secondLastName?: string;
          email?: string | null;
        };

        const fullName = [
          data.firstName,
          data.middleName,
          data.lastName,
          data.secondLastName,
        ]
          .filter((part) => typeof part === "string" && part.trim().length > 0)
          .map((part) => String(part).trim())
          .join(" ");

        return {
          userId: String(data.id),
          fullName,
          email: data.email ?? null,
        };
      } catch (error: any) {
        if (error?.code === "ECONNABORTED" || /timeout/i.test(String(error?.message ?? ""))) {
          const timeoutErr: any = new Error(
            "El servidor tardó demasiado en responder. Verificaremos si el usuario fue creado."
          );
          timeoutErr.code = "ECONNABORTED";
          timeoutErr.original = error;
          if (error?.response) timeoutErr.response = error.response;
          throw timeoutErr;
        }
        throw error;
      }
    },

    async listIdTypes(): Promise<CatalogItem[]> {
      const res = await api.get("/api/idtypes", { validateStatus: () => true });
      if (res.status !== 200) {
        throw new Error(`Catálogo idType HTTP ${res.status}`);
      }
      return Array.isArray(res.data)
        ? (res.data as any[]).map((item) => ({
            id: String(item?.id ?? ""),
            name: String(item?.name ?? ""),
          }))
        : [];
    },

    async listCountries(): Promise<CatalogItem[]> {
      const res = await api.get("/api/locations/countries", { validateStatus: () => true });
      if (res.status !== 200) {
        throw new Error(`Catálogo países HTTP ${res.status}`);
      }
      return Array.isArray(res.data)
        ? (res.data as any[]).map((item) => ({
            id: String(item?.id ?? ""),
            name: String(item?.name ?? ""),
          }))
        : [];
    },

    async listDepartments(countryId: string): Promise<CatalogItem[]> {
      const sanitized = countryId?.trim();
      if (!sanitized) {
        return [];
      }
      const res = await api.get(`/api/locations/countries/${encodeURIComponent(sanitized)}/departments`, {
        validateStatus: () => true,
      });
      if (res.status !== 200) {
        throw new Error(`Catálogo departamentos HTTP ${res.status}`);
      }
      return Array.isArray(res.data)
        ? (res.data as any[]).map((item) => ({
            id: String(item?.id ?? ""),
            name: String(item?.name ?? ""),
          }))
        : [];
    },

    async listCitiesByDepartment(departmentId: string): Promise<CatalogItem[]> {
      const sanitized = departmentId?.trim();
      if (!sanitized) {
        return [];
      }
      const res = await api.get(`/api/locations/departments/${encodeURIComponent(sanitized)}/cities`, {
        validateStatus: () => true,
      });
      if (res.status !== 200) {
        throw new Error(`Catálogo ciudades HTTP ${res.status}`);
      }
      return Array.isArray(res.data)
        ? (res.data as any[]).map((item) => ({
            id: String(item?.id ?? ""),
            name: String(item?.name ?? ""),
          }))
        : [];
    },

    async findUserLocally(params: { email?: string; idNumber?: string }): Promise<User | null> {
      const res = await api.get("/api/users", {
        params: { page: 0, size: 50 },
        validateStatus: () => true,
      });
      if (res.status !== 200) {
        return null;
      }
      const payload = res.data as { users?: any[] };
      const items = Array.isArray(payload?.users)
        ? payload.users.map(buildUserFromDto)
        : [];

      const targetEmail = params.email?.toLowerCase();
      const targetIdNumber = params.idNumber?.trim();

      return (
        items.find((user) => {
          if (targetEmail && user.email?.toLowerCase() === targetEmail) {
            return true;
          }
          if (targetIdNumber && user.idNumber === targetIdNumber) {
            return true;
          }
          return false;
        }) ?? null
      );
    },

    async requestEmailConfirmation(userId: string): Promise<ConfirmationRequestResult> {
      return requestConfirmation(userId, "email");
    },

    async requestMobileConfirmation(userId: string): Promise<ConfirmationRequestResult> {
      return requestConfirmation(userId, "mobile");
    },

    async validateEmailConfirmation(userId: string, code: string): Promise<VerificationAttemptResponse> {
      return verifyCode(userId, "email", code);
    },

    async validateMobileConfirmation(userId: string, code: string): Promise<VerificationAttemptResponse> {
      return verifyCode(userId, "mobile", code);
    },
  };
};
