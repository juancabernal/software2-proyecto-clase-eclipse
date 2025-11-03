// src/components/auth0/CallbackPage.tsx
import { useAuth0 } from "@auth0/auth0-react";

const CallbackPage = () => {
  const { isLoading, error } = useAuth0();

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-screen text-red-400">
        <h1 className="text-2xl font-bold mb-4">Error al iniciar sesión</h1>
        <p>{error.message}</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center h-screen text-gray-200">
      <h1 className="text-xl font-semibold mb-4">Procesando inicio de sesión...</h1>
      {isLoading && <p>Por favor espera un momento.</p>}
    </div>
  );
};

export default CallbackPage;
