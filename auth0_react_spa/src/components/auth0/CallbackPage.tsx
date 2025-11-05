// components/auth0/CallbackPage.tsx
import { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";

export default function CallbackPage() {
  const navigate = useNavigate();
  const { search } = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(search);
    const error = params.get("error");

    // Usuario canceló
    if (error === "access_denied") {
      navigate("/?login=cancelado", { replace: true });
    }
    // Auth0Provider.onRedirectCallback() manejará el caso exitoso
  }, [search, navigate]);

  return (
    <div className="flex items-center justify-center p-8 text-gray-200">
      Procesando inicio de sesión…
    </div>
  );
}
