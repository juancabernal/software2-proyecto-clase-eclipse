// Auth0ProviderWithNavigate.tsx
import { AppState, Auth0Provider } from "@auth0/auth0-react";
import { useNavigate } from "react-router-dom";
import { env } from "../../config/env";

type Props = { children: JSX.Element };

export const Auth0ProviderWithNavigate = ({ children }: Props) => {
  const navigate = useNavigate();

  const {
    auth0: { domain, clientId, callbackUrl, audience, scope },
  } = env;

  const onRedirectCallback = (appState: AppState | undefined) => {
    // Si no hay appState, por defecto vamos al dashboard (mejor UX que la raíz)
    navigate(appState?.returnTo || "/dashboard", { replace: true });
  };

  return (
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        audience,
        scope,
        redirect_uri: callbackUrl,
      }}
      cacheLocation="memory"  
      useRefreshTokens={true}   
      useRefreshTokensFallback={false}  
      onRedirectCallback={onRedirectCallback}
    >
      {children}
    </Auth0Provider>
  );
};
