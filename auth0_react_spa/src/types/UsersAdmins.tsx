import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { makeApi, Page, UserCreateInput } from "../api/client";
import { User } from "./users";

type Filters = {
  search: string;
  pais: string;
  estado: string;
  ciudad: string;
  sort: string;   // "primerApellido,asc" | "primerApellido,desc" | ""
  size: number;   // 10, 20, 50
  page: number;   // 1-based
};

const initialFilters: Filters = {
  search: "",
  pais: "",
  estado: "",
  ciudad: "",
  sort: "primerApellido,asc",
  size: 10,
  page: 1,
};

export default function UsersAdmin() {
  const { getAccessTokenSilently } = useAuth0();

  const baseURL = import.meta.env.VITE_API_SERVER_URL as string;
  const audience = import.meta.env.VITE_AUTH0_AUDIENCE as string;

  const api = useMemo(
    () => makeApi(baseURL, async () => {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience },
        // scope: "admin:read users:write", // si aplica
        // cacheMode: "off",
      });
      return token;
    }),
    [baseURL, audience, getAccessTokenSilently]
  );

  const [filters, setFilters] = useState<Filters>(initialFilters);
  const [pageData, setPageData] = useState<Page<User> | null>(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  // Modal “Nuevo usuario”
  const [openNew, setOpenNew] = useState(false);
  const [creating, setCreating] = useState(false);
  const emptyForm = (): UserCreateInput => ({
    primerNombre: "",
    segundoNombre: "",
    primerApellido: "",
    segundoApellido: "",
    correo: "",
    telefono: "",
    ciudad: "",
    estado: "",
    pais: "",
  });

  const [form, setForm] = useState<UserCreateInput>(emptyForm);
  const [formErr, setFormErr] = useState<string | null>(null);

  const resetForm = () => setForm(emptyForm());

  // Fetch usuarios
  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      setErr(null);
      const data = await api.listUsers({
        page: filters.page,
        size: filters.size,
        search: filters.search || undefined,
        country: filters.pais || undefined,
        state: filters.estado || undefined,
        city: filters.ciudad || undefined,
        sort: filters.sort || undefined,
      });
      setPageData(data);
    } catch (e: any) {
      setErr(e?.message || "No se pudo cargar usuarios.");
    } finally {
      setLoading(false);
    }
  }, [api, filters]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // Helpers
  const fullName = (u: User) =>
    [u.primerNombre, u.segundoNombre, u.primerApellido, u.segundoApellido]
      .filter(Boolean)
      .join(" ");

  const onChangeInput = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFilters((f) => ({
      ...f,
      [name]: name === "size" ? Number(value) : value,
      ...(name !== "page" ? { page: 1 } : {}), // cualquier cambio reinicia a página 1
    }));
  };

  const nextPage = () => {
    if (!pageData) return;
    if (filters.page < pageData.totalPages) {
      setFilters((f) => ({ ...f, page: f.page + 1 }));
    }
  };
  const prevPage = () => {
    if (filters.page > 1) {
      setFilters((f) => ({ ...f, page: f.page - 1 }));
    }
  };

  const resetFilters = () => setFilters(initialFilters);

  // Crear usuario
  const validateForm = (f: UserCreateInput) => {
    if (!f.primerNombre?.trim()) return "Primer nombre es obligatorio";
    if (!f.primerApellido?.trim()) return "Primer apellido es obligatorio";
    if (!f.correo?.trim()) return "Correo es obligatorio";
    // validación simple de email
    if (!/^\S+@\S+\.\S+$/.test(f.correo)) return "Correo inválido";
    return null;
  };

  const createUser = async () => {
    const v = validateForm(form);
    if (v) { setFormErr(v); return; }
    setFormErr(null);
    try {
      setCreating(true);
      await api.createUser(form);
      setOpenNew(false);
      resetForm();
      // recarga página 1 para ver el nuevo
      setFilters((f) => ({ ...f, page: 1 }));
    } catch (e: any) {
      setFormErr(e?.message || "No se pudo crear el usuario.");
    } finally {
      setCreating(false);
    }
  };

  return (
    <section className="space-y-6">
      {/* Toolbar */}
      <div className="rounded-2xl border border-gray-800 bg-[#141418] p-4">
        <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
          <div className="grid grid-cols-1 gap-3 md:grid-cols-5">
            <input
              name="search"
              value={filters.search}
              onChange={onChangeInput}
              placeholder="Buscar por nombre o correo…"
              className="col-span-2 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
            />
            <input
              name="pais"
              value={filters.pais}
              onChange={onChangeInput}
              placeholder="País"
              className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
            />
            <input
              name="estado"
              value={filters.estado}
              onChange={onChangeInput}
              placeholder="Estado/Provincia"
              className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
            />
            <input
              name="ciudad"
              value={filters.ciudad}
              onChange={onChangeInput}
              placeholder="Ciudad"
              className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
            />
          </div>

          <div className="flex items-center gap-3">
            <select
              name="sort"
              value={filters.sort}
              onChange={onChangeInput}
              className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
            >
              <option value="primerApellido,asc">Apellido ↑</option>
              <option value="primerApellido,desc">Apellido ↓</option>
              <option value="primerNombre,asc">Nombre ↑</option>
              <option value="primerNombre,desc">Nombre ↓</option>
            </select>

            <select
              name="size"
              value={filters.size}
              onChange={onChangeInput}
              className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
            >
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>

            <button
              onClick={() => setOpenNew(true)}
              className="rounded-lg bg-gradient-to-r from-indigo-500 via-blue-500 to-purple-600 px-4 py-2 text-sm font-medium text-white transition hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-purple-600"
            >
              + Nuevo usuario
            </button>

            <button
              onClick={resetFilters}
              className="rounded-lg border border-gray-700 px-4 py-2 text-sm font-medium text-gray-200 transition hover:text-white hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-600"
            >
              Limpiar
            </button>
          </div>
        </div>
      </div>

      {/* Tabla */}
      <div className="overflow-hidden rounded-2xl border border-gray-800">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-800">
            <thead className="bg-[#141418]">
              <tr className="text-left text-xs font-semibold uppercase tracking-wider text-gray-400">
                <th className="px-4 py-3">Nombre</th>
                <th className="px-4 py-3">Correo</th>
                <th className="px-4 py-3">Teléfono</th>
                <th className="px-4 py-3">Ciudad</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3">País</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800 bg-[#0f0f12]">
              {loading && (
                <tr>
                  <td colSpan={6} className="px-4 py-6 text-center text-sm text-gray-400">
                    Cargando…
                  </td>
                </tr>
              )}

              {!loading && err && (
                <tr>
                  <td colSpan={6} className="px-4 py-6 text-center text-sm text-red-300">
                    {err}
                  </td>
                </tr>
              )}

              {!loading && !err && pageData?.items?.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-6 text-center text-sm text-gray-400">
                    Sin resultados
                  </td>
                </tr>
              )}

              {!loading && !err && pageData?.items?.map((u) => (
                <tr key={u.id} className="hover:bg-[#121217]">
                  <td className="px-4 py-3 text-sm text-gray-100">{fullName(u)}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{u.correo}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{u.telefono || "—"}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{u.ciudad || "—"}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{u.estado || "—"}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{u.pais || "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Paginación */}
        <div className="flex items-center justify-between bg-[#141418] px-4 py-3">
          <div className="text-xs text-gray-400">
            {pageData
              ? `Mostrando página ${filters.page} de ${pageData.totalPages} · ${pageData.totalItems} usuarios`
              : "—"}
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={prevPage}
              disabled={!pageData || filters.page <= 1}
              className="rounded-lg border border-gray-700 px-3 py-1.5 text-sm text-gray-200 disabled:opacity-40 hover:text-white hover:border-gray-500"
            >
              ← Anterior
            </button>
            <button
              onClick={nextPage}
              disabled={!pageData || filters.page >= (pageData?.totalPages || 1)}
              className="rounded-lg border border-gray-700 px-3 py-1.5 text-sm text-gray-200 disabled:opacity-40 hover:text-white hover:border-gray-500"
            >
              Siguiente →
            </button>
          </div>
        </div>
      </div>

      {/* Modal nuevo usuario */}
      {openNew && (
        <div className="fixed inset-0 z-50 grid place-items-center bg-black/50 px-4">
          <div className="w-full max-w-2xl rounded-2xl border border-gray-800 bg-[#141418] p-6">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-white">Registrar nuevo usuario</h3>
              <button
                onClick={() => {
                  setOpenNew(false);
                  resetForm();
                  setFormErr(null);
                }}
                className="rounded-lg border border-gray-700 px-2 py-1 text-sm text-gray-200 hover:text-white hover:border-gray-500"
              >
                Cerrar
              </button>
            </div>

            <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-2">
              <input
                placeholder="Primer nombre *"
                value={form.primerNombre}
                onChange={(e) => setForm((f) => ({ ...f, primerNombre: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="Segundo nombre"
                value={form.segundoNombre}
                onChange={(e) => setForm((f) => ({ ...f, segundoNombre: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="Primer apellido *"
                value={form.primerApellido}
                onChange={(e) => setForm((f) => ({ ...f, primerApellido: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="Segundo apellido"
                value={form.segundoApellido}
                onChange={(e) => setForm((f) => ({ ...f, segundoApellido: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="Correo *"
                type="email"
                value={form.correo}
                onChange={(e) => setForm((f) => ({ ...f, correo: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="Teléfono"
                value={form.telefono}
                onChange={(e) => setForm((f) => ({ ...f, telefono: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="País"
                value={form.pais}
                onChange={(e) => setForm((f) => ({ ...f, pais: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="Estado/Provincia"
                value={form.estado}
                onChange={(e) => setForm((f) => ({ ...f, estado: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
              <input
                placeholder="Ciudad"
                value={form.ciudad}
                onChange={(e) => setForm((f) => ({ ...f, ciudad: e.target.value }))}
                className="rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              />
            </div>

            {formErr && (
              <p className="mt-3 text-sm text-red-300">{formErr}</p>
            )}

            <div className="mt-5 flex items-center justify-end gap-3">
              <button
                onClick={() => setOpenNew(false)}
                className="rounded-lg border border-gray-700 px-4 py-2 text-sm font-medium text-gray-200 hover:text-white hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-600"
              >
                Cancelar
              </button>
              <button
                onClick={createUser}
                disabled={creating}
                className="rounded-lg bg-gradient-to-r from-indigo-500 via-blue-500 to-purple-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
              >
                {creating ? "Creando…" : "Crear usuario"}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}