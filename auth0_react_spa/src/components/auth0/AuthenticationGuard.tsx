// components/auth0/AuthenticationGuard.tsx
import { withAuthenticationRequired, useAuth0 } from "@auth0/auth0-react";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

type Props = {
  component: React.ComponentType<object>;
};

export const AuthenticationGuard = ({ component }: Props) => {
  const Component = withAuthenticationRequired(component, {
    onRedirecting: () => (
      <div className="flex flex-col items-center justify-center mx-96">
        <div className="mb-4 text-2xl font-bold">Redireccionando...</div>
      </div>
    ),
  });

  // 👇 Manejo de error global (cancelado u otros)
  const { error, isLoading } = useAuth0();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && error) {
      const err = error as any;
      if (err?.error === "access_denied") {
        navigate("/?login=cancelado", { replace: true });
      } else {
        navigate("/?login=error", { replace: true });
      }
    }
  }, [error, isLoading, navigate]);

  return <Component />;
};
