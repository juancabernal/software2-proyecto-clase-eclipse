import Header from "../components/header/Header";
import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

export default function Home() {
  const { loginWithRedirect, isAuthenticated, error, isLoading } = useAuth0();
  const [redirecting, setRedirecting] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  // Lee flags de la URL (/?login=cancelado | /?login=error)
  const loginStatus = useMemo(() => {
    const params = new URLSearchParams(location.search);
    const v = params.get("login");
    return v === "cancelado" ? "cancelado" : v === "error" ? "error" : null;
  }, [location.search]);

  // Limpia el query una vez leído, para no dejar ?login=... pegado
  useEffect(() => {
    if (loginStatus) {
      const params = new URLSearchParams(location.search);
      params.delete("login");
      navigate({ pathname: location.pathname, search: params.toString() }, { replace: true });
    }
  }, [loginStatus, location.pathname, location.search, navigate]);

  // Manejo defensivo por si el SDK expone error global (además de tu CallbackPage)
  useEffect(() => {
    if (!isLoading && error) {
      const err = error as any;
      if (err?.error === "access_denied") {
        // ya estarías en / con ?login=cancelado (según tu flujo),
        // pero si llegas por otra ruta, navega y muestra banner.
        navigate("/?login=cancelado", { replace: true });
      } else {
        navigate("/?login=error", { replace: true });
      }
    }
  }, [error, isLoading, navigate]);

  const handleLogin = async () => {
    try {
      setRedirecting(true);
      await loginWithRedirect({
        appState: { returnTo: "/auth/gate" }, // o "/" si prefieres decidir en Home
      });
    } finally {
      setRedirecting(false);
    }
  };

  return (
    <>
      <Header subtitle="Inicio" isAuthenticated={isAuthenticated} />

      <div className="min-h-screen bg-[#0f0f12] text-gray-100">
        {/* ALERTAS */}
        {loginStatus && (
          <div
            role="alert"
            className={`border-b px-4 py-3 text-center text-sm ${
              loginStatus === "cancelado"
                ? "bg-yellow-900/30 border-yellow-700 text-yellow-200"
                : "bg-red-900/30 border-red-700 text-red-200"
            }`}
          >
            {loginStatus === "cancelado"
              ? "Iniciaste sesión y cancelaste en Auth0. No se realizaron cambios."
              : "Ocurrió un error al iniciar sesión. Inténtalo de nuevo."}
          </div>
        )}

        {/* HERO / CONTENIDO */}
        <main className="mx-auto max-w-6xl px-4 pb-20 pt-16 lg:pt-24">
          <section className="grid items-center gap-12 lg:grid-cols-2">
            {/* Texto principal */}
            <div>
              <h1 className="text-4xl font-extrabold leading-tight tracking-tight text-white sm:text-5xl">
                Bienvenido a{" "}
                <span className="bg-gradient-to-r from-indigo-400 via-blue-400 to-purple-500 bg-clip-text text-transparent">
                  Uco Challenge
                </span>
              </h1>
              <p className="mt-6 text-lg text-gray-300">
                Gestiona, analiza y visualiza toda la información de los retos de UCO. Esta
                plataforma está diseñada para brindar una experiencia clara, moderna y potente —
                completamente personalizable.
              </p>

              <ul className="mt-8 space-y-3 text-gray-300">
                <li className="flex items-start gap-3">
                  <span className="mt-1 inline-block h-2 w-2 rounded-full bg-gradient-to-r from-indigo-500 via-blue-500 to-purple-500"></span>
                  Plataforma centralizada y segura.
                </li>
                <li className="flex items-start gap-3">
                  <span className="mt-1 inline-block h-2 w-2 rounded-full bg-gradient-to-r from-indigo-500 via-blue-500 to-purple-500"></span>
                  Acceso rápido a paneles administrativos.
                </li>
                <li className="flex items-start gap-3">
                  <span className="mt-1 inline-block h-2 w-2 rounded-full bg-gradient-to-r from-indigo-500 via-blue-500 to-purple-500"></span>
                  Integración completa con Auth0.
                </li>
              </ul>

              <div className="mt-10">
                <button
                  onClick={handleLogin}
                  disabled={redirecting}
                  className={`rounded-lg bg-gradient-to-r from-indigo-500 via-blue-500 to-purple-600 px-6 py-3 text-sm font-semibold text-white shadow-md transition focus:outline-none focus:ring-2 focus:ring-purple-500 ${
                    redirecting ? "opacity-60 cursor-not-allowed" : "hover:opacity-90"
                  }`}
                  aria-describedby="login-help"
                >
                  {redirecting ? "Redirigiendo…" : "Iniciar sesión"}
                </button>
                <p id="login-help" className="mt-2 text-sm text-gray-400">
                  Accede con tu cuenta para continuar.
                </p>
              </div>
            </div>

            {/* Imagen decorativa */}
            <div className="relative">
              {/* Halo decorativo */}
              <div
                aria-hidden="true"
                className="pointer-events-none absolute -left-8 -top-8 -z-10 h-56 w-56 rounded-full bg-gradient-to-r from-indigo-600 via-blue-600 to-purple-700 opacity-40 blur-3xl sm:h-72 sm:w-72"
              />
              <div className="overflow-hidden rounded-2xl border border-gray-700 bg-[#1a1a1f] shadow-lg">
                <img
                  src="Logo-02-1.png"
                  alt="Ilustración decorativa"
                  className="h-full w-full object-cover opacity-90"
                />
              </div>
              <p className="mt-4 text-center text-sm text-gray-400">¡Acreditada en alta calidad!</p>
            </div>
          </section>
        </main>

        {/* FOOTER */}
        <footer className="border-t border-gray-800 bg-[#141418]">
          <div className="mx-auto max-w-6xl px-4 py-8">
            <p className="text-center text-sm text-gray-500">
              © {new Date().getFullYear()} Uco Challenge. Información descriptiva del
              proyecto. Este texto es editable y no contiene enlaces.
            </p>
          </div>
        </footer>
      </div>
    </>
  );
}
