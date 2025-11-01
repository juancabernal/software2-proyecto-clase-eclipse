import axios from "axios";
import { User } from "../types/users";

export type Page<T> = {
  items: T[];
  page: number;        // 1-based
  size: number;        // page size
  totalItems: number;  // total rows
  totalPages: number;  // ceil(totalItems/size)
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
      search?: string;
      country?: string;
      state?: string;
      city?: string;
      sort?: string; // e.g. "primerApellido,asc"
    }): Promise<Page<User>> {
      const res = await api.get("/api/admin/users", {
        params,
        validateStatus: () => true,
      });
      if (res.status !== 200) {
        throw new Error(`Listado usuarios HTTP ${res.status}`);
      }
      return res.data as Page<User>;
    },

    // POST /api/admin/users
    async createUser(payload: UserCreateInput): Promise<User> {
      const res = await api.post("/api/admin/users", payload, {
        validateStatus: () => true,
      });
      if (res.status !== 201) {
        const msg = (res.data && (res.data.message || res.data.error)) || "No se pudo crear el usuario";
        throw new Error(msg);
      }
      return res.data as User;
    },
  };
};

// Tipos
export type UserCreateInput = {
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  correo: string;
  telefono?: string;
  ciudad?: string;
  estado?: string;
  pais?: string;
};
