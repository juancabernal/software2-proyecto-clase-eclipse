import axios from "axios";
import { User } from "../types/users";

export type ApiSuccessResponse<T> = {
  userMessage: string;
  data: T;
};

export type Page<T> = {
  items: T[];
  page: number;        // 1-based
  size: number;        // page size
  totalItems: number;  // total rows
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

    async listDepartments(): Promise<CatalogItem[]> {
      const res = await api.get("/api/admin/catalogs/departments", { validateStatus: () => true });
      if (res.status !== 200) {
        throw new Error(`Catálogo departamentos HTTP ${res.status}`);
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

    async listCitiesByDepartment(departmentId: string): Promise<CatalogItem[]> {
      const res = await api.get(`/api/admin/catalogs/departments/${departmentId}/cities`, {
        validateStatus: () => true,
      });
      if (res.status !== 200) {
        throw new Error(`Catálogo ciudades por departamento HTTP ${res.status}`);
      }
      const payload = res.data as ApiSuccessResponse<CatalogItem[]>;
      return payload.data;
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
