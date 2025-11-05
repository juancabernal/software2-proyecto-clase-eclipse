import { useEffect, useMemo, useState } from "react"; // ✅ FIX: Manage lifecycle and memoized API instance
import { useNavigate, useSearchParams } from "react-router-dom"; // ✅ FIX: Read query parameters and provide navigation helpers

import { makeApi, VerificationAttemptResponse } from "../api/client"; // ✅ FIX: Reuse shared API client utilities

const VerifyAccount = () => { // ✅ FIX: Render standalone verification page
  const [searchParams] = useSearchParams(); // ✅ FIX: Access URL query parameters
  const navigate = useNavigate(); // ✅ FIX: Enable programmatic navigation
  const baseURL = import.meta.env.VITE_API_SERVER_URL as string; // ✅ FIX: Resolve API base URL from environment
  const api = useMemo(() => makeApi(baseURL, async () => ""), [baseURL]); // ✅ FIX: Create API client without requiring authentication

  const [loading, setLoading] = useState(true); // ✅ FIX: Track request progress for UI feedback
  const [result, setResult] = useState<VerificationAttemptResponse | null>(null); // ✅ FIX: Store backend verification response
  const [error, setError] = useState<string | null>(null); // ✅ FIX: Capture error messages for user display

  useEffect(() => { // ✅ FIX: Trigger verification request on initial render
    const token = searchParams.get("token") || searchParams.get("verificationId") || ""; // ✅ FIX: Support multiple query aliases for token
    if (!token) { // ✅ FIX: Handle missing token gracefully
      setError("No se encontró un token de verificación en el enlace."); // ✅ FIX: Inform user about missing token parameter
      setLoading(false); // ✅ FIX: Stop loading state when token absent
      return; // ✅ FIX: Skip backend call without token
    }

    let isMounted = true; // ✅ FIX: Avoid state updates on unmounted component
    setLoading(true); // ✅ FIX: Start loading indicator before backend call
    api.verifyUserToken(token) // ✅ FIX: Request backend verification using provided token
      .then((response) => {
        if (!isMounted) return; // ✅ FIX: Prevent race conditions on unmount
        setResult(response); // ✅ FIX: Persist successful verification payload
        setError(null); // ✅ FIX: Clear previous errors upon success
      })
      .catch((err: any) => {
        if (!isMounted) return; // ✅ FIX: Prevent state updates after unmount
        const message = err?.message || "No fue posible validar el token."; // ✅ FIX: Normalize error message for display
        setError(message); // ✅ FIX: Store error message for rendering
        setResult(null); // ✅ FIX: Clear stale result after failure
      })
      .finally(() => {
        if (isMounted) { // ✅ FIX: Ensure component still mounted before updating state
          setLoading(false); // ✅ FIX: Stop loading indicator after request completes
        }
      });

    return () => { // ✅ FIX: Cleanup flag on component unmount
      isMounted = false; // ✅ FIX: Toggle guard to avoid setState on unmounted component
    };
  }, [api, searchParams]);

  const navigateHome = () => navigate("/"); // ✅ FIX: Provide quick navigation back to landing page
  const navigateDashboard = () => navigate("/dashboard"); // ✅ FIX: Provide navigation towards authenticated dashboard

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center px-4"> {/* ✅ FIX: Apply Tailwind styling for centered layout */}
      <div className="w-full max-w-lg rounded-xl border border-slate-800 bg-slate-900/70 p-8 shadow-2xl shadow-slate-900/40"> {/* ✅ FIX: Create styled verification card */}
        <h1 className="text-2xl font-semibold text-emerald-300">Verificación de cuenta</h1> {/* ✅ FIX: Present page heading */}
        <p className="mt-2 text-sm text-slate-300">
          {/* ✅ FIX: Describe verification status to the user */}
          Confirma tu correo o número usando el enlace enviado por UCOChallenge.
        </p>

        {loading && (
          <div className="mt-6 rounded-lg border border-slate-800 bg-slate-900/60 p-4 text-sm text-slate-200"> {/* ✅ FIX: Display loading message */}
            Validando tu token, por favor espera…
          </div>
        )}

        {!loading && error && (
          <div className="mt-6 rounded-lg border border-rose-600/60 bg-rose-500/10 p-4 text-sm text-rose-200"> {/* ✅ FIX: Display error state */}
            {error}
          </div>
        )}

        {!loading && !error && result && (
          <div className="mt-6 rounded-lg border border-emerald-600/50 bg-emerald-500/10 p-4 text-sm text-emerald-200"> {/* ✅ FIX: Display success feedback */}
            <p className="font-medium">{result.message || "La verificación se completó correctamente."}</p>
            <p className="mt-2 text-xs text-emerald-300/80"> {/* ✅ FIX: Highlight contact confirmation status */}
              Estado del contacto: {result.contactConfirmed ? "Confirmado" : "Pendiente"}
            </p>
            <p className="mt-1 text-xs text-emerald-300/80"> {/* ✅ FIX: Highlight global confirmation status */}
              Estado general de la cuenta: {result.allContactsConfirmed ? "Verificada" : "En progreso"}
            </p>
          </div>
        )}

        {!loading && !error && result && !result.success && (
          <p className="mt-4 text-xs text-amber-300/80"> {/* ✅ FIX: Inform user about remaining attempts when validation fails */}
            Intentos restantes: {result.attemptsRemaining}
          </p>
        )}

        <div className="mt-8 flex flex-col gap-3 sm:flex-row"> {/* ✅ FIX: Layout action buttons */}
          <button
            type="button"
            onClick={navigateHome}
            className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-slate-100 transition hover:bg-slate-700"
          >
            Volver al inicio
          </button>
          <button
            type="button"
            onClick={navigateDashboard}
            className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-semibold text-slate-900 transition hover:bg-emerald-400"
          >
            Ir al panel
          </button>
        </div>
      </div>
    </div>
  );
};

export default VerifyAccount; // ✅ FIX: Export verification view for router usage
