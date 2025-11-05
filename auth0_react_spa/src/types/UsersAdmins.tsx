import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";

import {
  CatalogItem,
  Page,
  RegisterUserResponse,
  UserCreateInput,
  makeApi,
} from "../api/client";
import { User } from "./users";

type Filters = {
  page: number;
  size: number;
};

type UserFormState = {
  idType: string;
  idNumber: string;
  firstName: string;
  secondName: string;
  firstSurname: string;
  secondSurname: string;
  homeCity: string;
  email: string;
  mobileNumber: string;
};

const initialFilters: Filters = {
  page: 1,
  size: 10,
};

const emptyForm = (): UserFormState => ({
  idType: "",
  idNumber: "",
  firstName: "",
  secondName: "",
  firstSurname: "",
  secondSurname: "",
  homeCity: "",
  email: "",
  mobileNumber: "",
});

const buildPayload = (form: UserFormState): UserCreateInput => {
  const sanitize = (value: string) => value.trim();
  const basePayload: UserCreateInput = {
    idType: sanitize(form.idType),
    idNumber: sanitize(form.idNumber),
    firstName: sanitize(form.firstName),
    firstSurname: sanitize(form.firstSurname),
    homeCity: sanitize(form.homeCity),
    email: sanitize(form.email),
  };

  const extras: Partial<UserCreateInput> = {};
  const secondName = sanitize(form.secondName);
  if (secondName) {
    extras.secondName = secondName;
  }
  const secondSurname = sanitize(form.secondSurname);
  if (secondSurname) {
    extras.secondSurname = secondSurname;
  }
  const mobile = sanitize(form.mobileNumber);
  if (mobile) {
    extras.mobileNumber = mobile;
  }

  return { ...basePayload, ...extras };
};

export default function UsersAdmin() {
  const { getAccessTokenSilently } = useAuth0();

  const baseURL = import.meta.env.VITE_API_SERVER_URL as string;
  const audience = import.meta.env.VITE_AUTH0_AUDIENCE as string;

  const api = useMemo(
    () =>
      makeApi(baseURL, async () => {
        const token = await getAccessTokenSilently({
          authorizationParams: { audience },
        });
        return token;
      }),
    [baseURL, audience, getAccessTokenSilently]
  );

  const [filters, setFilters] = useState<Filters>(initialFilters);
  const [pageData, setPageData] = useState<Page<User> | null>(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const [openNew, setOpenNew] = useState(false);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState<UserFormState>(emptyForm);
  const [formErr, setFormErr] = useState<string | null>(null);
  const [creationResult, setCreationResult] = useState<RegisterUserResponse | null>(null);

  const [idTypes, setIdTypes] = useState<CatalogItem[]>([]);
  const [departments, setDepartments] = useState<CatalogItem[]>([]);
  const [selectedDepartment, setSelectedDepartment] = useState<string>("");
  const [cities, setCities] = useState<CatalogItem[]>([]);
  const [catalogErr, setCatalogErr] = useState<string | null>(null);
  const [catalogLoading, setCatalogLoading] = useState(false);

  const resetForm = () => {
    setForm(emptyForm());
    setFormErr(null);
    setSelectedDepartment("");
  };

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      setErr(null);
      const data = await api.listUsers({
        page: filters.page,
        size: filters.size,
      });
      setPageData(data);
    } catch (e: any) {
      setErr(e?.message || "No se pudo cargar usuarios.");
    } finally {
      setLoading(false);
    }
  }, [api, filters.page, filters.size]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  useEffect(() => {
    let active = true;
    const loadCatalogs = async () => {
      try {
        setCatalogLoading(true);
        setCatalogErr(null);
        const [idTypeOptions, departmentOptions] = await Promise.all([
          api.listIdTypes(),
          api.listDepartments(),
        ]);
        if (!active) return;
        setIdTypes(idTypeOptions);
        setDepartments(departmentOptions);
      } catch (error: any) {
        if (active) {
          setCatalogErr(error?.message || "No se pudieron cargar los catálogos.");
        }
      } finally {
        if (active) {
          setCatalogLoading(false);
        }
      }
    };
    loadCatalogs();
    return () => {
      active = false;
    };
  }, [api]);

  useEffect(() => {
    if (!selectedDepartment) {
      setCities([]);
      return;
    }
    let active = true;
    const loadCities = async () => {
      try {
        setCatalogLoading(true);
        setCatalogErr(null);
        const cityOptions = await api.listCitiesByDepartment(selectedDepartment);
        if (active) {
          setCities(cityOptions);
        }
      } catch (error: any) {
        if (active) {
          setCatalogErr(error?.message || "No se pudieron cargar las ciudades.");
        }
      } finally {
        if (active) {
          setCatalogLoading(false);
        }
      }
    };
    loadCities();
    return () => {
      active = false;
    };
  }, [selectedDepartment, api]);

  const onChangePageSize = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = Number(e.target.value);
    setFilters((prev) => ({ ...prev, size: value, page: 1 }));
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

  const validateForm = (state: UserFormState) => {
    if (!state.idType.trim()) return "Selecciona el tipo de identificación.";
    if (!state.idNumber.trim()) return "Ingresa el número de identificación.";
    if (!state.firstName.trim()) return "El primer nombre es obligatorio.";
    if (!state.firstSurname.trim()) return "El primer apellido es obligatorio.";
    if (!state.homeCity.trim()) return "Selecciona la ciudad de residencia.";
    if (!state.email.trim()) return "El correo es obligatorio.";
    if (!/^\S+@\S+\.\S+$/.test(state.email.trim())) return "Correo inválido.";
    return null;
  };

  const createUser = async () => {
    const validation = validateForm(form);
    if (validation) {
      setFormErr(validation);
      return;
    }

    try {
      setFormErr(null);
      setCreating(true);
      const payload = buildPayload(form);
      const result = await api.createUser(payload);
      setCreationResult(result);
      setOpenNew(false);
      resetForm();
      setFilters((f) => ({ ...f, page: 1 }));
    } catch (error: any) {
      setFormErr(error?.message || "No se pudo crear el usuario.");
    } finally {
      setCreating(false);
    }
  };

  return (
    <section className="space-y-6">
      <div className="rounded-2xl border border-gray-800 bg-[#141418] p-4">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div className="text-sm text-gray-400">
            {pageData
              ? `Mostrando ${pageData.items.length} usuarios de ${pageData.totalItems}`
              : "Sin datos"}
          </div>
          <div className="flex items-center gap-3">
            <label className="text-sm text-gray-300">
              Tamaño página
              <select
                name="size"
                value={filters.size}
                onChange={onChangePageSize}
                className="ml-2 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
              >
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </label>

            <button
              onClick={() => {
                setOpenNew(true);
                setCreationResult(null);
              }}
              className="rounded-lg bg-gradient-to-r from-indigo-500 via-blue-500 to-purple-600 px-4 py-2 text-sm font-medium text-white transition hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-purple-600"
            >
              + Nuevo usuario
            </button>
          </div>
        </div>
      </div>

      <div className="overflow-hidden rounded-2xl border border-gray-800">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-800">
            <thead className="bg-[#141418]">
              <tr className="text-left text-xs font-semibold uppercase tracking-wider text-gray-400">
                <th className="px-4 py-3">Nombre</th>
                <th className="px-4 py-3">Correo</th>
                <th className="px-4 py-3">Teléfono</th>
                <th className="px-4 py-3">Tipo identificación</th>
                <th className="px-4 py-3">Número identificación</th>
                <th className="px-4 py-3">Confirmaciones</th>
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

              {!loading && !err && pageData?.items?.map((user) => (
                <tr key={user.userId} className="hover:bg-[#121217]">
                  <td className="px-4 py-3 text-sm text-gray-100">{user.fullName}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{user.email}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{user.mobileNumber || "—"}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{user.idType}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">{user.idNumber}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">
                    <span className={user.emailConfirmed ? "text-emerald-400" : "text-yellow-400"}>
                      Correo {user.emailConfirmed ? "confirmado" : "pendiente"}
                    </span>
                    <span className="mx-1">·</span>
                    <span className={user.mobileNumberConfirmed ? "text-emerald-400" : "text-yellow-400"}>
                      Móvil {user.mobileNumberConfirmed ? "confirmado" : "pendiente"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="flex items-center justify-between bg-[#141418] px-4 py-3">
          <div className="text-xs text-gray-400">
            {pageData
              ? `Mostrando página ${filters.page} de ${pageData.totalPages}`
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

      {openNew && (
        <div className="fixed inset-0 z-50 grid place-items-center bg-black/50 px-4">
          <div className="w-full max-w-3xl rounded-2xl border border-gray-800 bg-[#141418] p-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-white">Registrar nuevo usuario</h3>
                {catalogErr && (
                  <p className="mt-1 text-sm text-yellow-400">{catalogErr}</p>
                )}
              </div>
              <button
                onClick={() => {
                  setOpenNew(false);
                  resetForm();
                  setCreationResult(null);
                }}
                className="rounded-lg border border-gray-700 px-2 py-1 text-sm text-gray-200 hover:text-white hover:border-gray-500"
              >
                Cerrar
              </button>
            </div>

            <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-2">
              <label className="flex flex-col text-sm text-gray-300">
                Tipo de identificación *
                <select
                  value={form.idType}
                  disabled={catalogLoading}
                  onChange={(e) => setForm((f) => ({ ...f, idType: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                >
                  <option value="">Selecciona…</option>
                  {idTypes.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.name}
                    </option>
                  ))}
                </select>
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Número de identificación *
                <input
                  value={form.idNumber}
                  onChange={(e) => setForm((f) => ({ ...f, idNumber: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                />
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Primer nombre *
                <input
                  value={form.firstName}
                  onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                />
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Segundo nombre
                <input
                  value={form.secondName}
                  onChange={(e) => setForm((f) => ({ ...f, secondName: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                />
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Primer apellido *
                <input
                  value={form.firstSurname}
                  onChange={(e) => setForm((f) => ({ ...f, firstSurname: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                />
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Segundo apellido
                <input
                  value={form.secondSurname}
                  onChange={(e) => setForm((f) => ({ ...f, secondSurname: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                />
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Departamento *
                <select
                  value={selectedDepartment}
                  disabled={catalogLoading}
                  onChange={(e) => {
                    const dept = e.target.value;
                    setSelectedDepartment(dept);
                    setForm((f) => ({ ...f, homeCity: "" }));
                  }}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                >
                  <option value="">Selecciona…</option>
                  {departments.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.name}
                    </option>
                  ))}
                </select>
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Ciudad de residencia *
                <select
                  value={form.homeCity}
                  disabled={catalogLoading || !selectedDepartment}
                  onChange={(e) => setForm((f) => ({ ...f, homeCity: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                >
                  <option value="">Selecciona…</option>
                  {cities.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.name}
                    </option>
                  ))}
                </select>
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Correo electrónico *
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                />
              </label>
              <label className="flex flex-col text-sm text-gray-300">
                Teléfono móvil
                <input
                  value={form.mobileNumber}
                  onChange={(e) => setForm((f) => ({ ...f, mobileNumber: e.target.value }))}
                  className="mt-1 rounded-lg border border-gray-700 bg-[#0f0f12] px-3 py-2 text-sm text-gray-100 outline-none focus:border-gray-500"
                />
              </label>
            </div>

            {formErr && <p className="mt-3 text-sm text-red-300">{formErr}</p>}

            <div className="mt-5 flex items-center justify-end gap-3">
              <button
                onClick={() => {
                  setOpenNew(false);
                  resetForm();
                }}
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

      {creationResult && (
        <div className="rounded-xl border border-emerald-800 bg-emerald-900/30 px-4 py-3 text-sm text-emerald-200">
          Usuario <strong>{creationResult.fullName}</strong> registrado con ID {creationResult.userId}.
        </div>
      )}
    </section>
  );
}
