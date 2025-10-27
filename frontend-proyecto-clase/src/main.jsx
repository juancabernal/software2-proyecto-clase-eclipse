import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { Auth0Provider } from '@auth0/auth0-react';

import App from './App';

// estilos globales (asegúrate de tener estos archivos)
import './index.css';
import './App.css';

/**
 * Punto de entrada de la app.
 * Usamos BrowserRouter aquí para que toda la app tenga routing.
 */
const container = document.getElementById('root');
const root = createRoot(container);

const domain = "dev-x2nlunlga02cbz17.us.auth0.com";
const clientId = "h5qDo7ps7iaJSXXmD87GtUIodCzMjaaf";
const audience = import.meta.env.VITE_AUTH0_AUDIENCE;
const extraScope = import.meta.env.VITE_AUTH0_SCOPE || import.meta.env.VITE_AUTH0_API_SCOPE;
const defaultScopes = ['openid', 'profile', 'email'];
const scopes = [...defaultScopes, ...(extraScope ? extraScope.split(' ') : [])]
  .filter(Boolean)
  .join(' ');

if (!domain || !clientId) {
  console.warn('Auth0 configuration is missing domain and/or client id.');
}

// Debug: mostrar variables de entorno (sin exponer secrets)
console.log('VITE_AUTH0_DOMAIN=', domain);
console.log('VITE_AUTH0_CLIENT_ID=', clientId);
console.log('VITE_AUTH0_AUDIENCE=', audience);

root.render(
  <React.StrictMode>
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience: import.meta.env.VITE_AUTH0_AUDIENCE,
        scope: scopes
      }}
      useRefreshTokens={true}
      cacheLocation="localstorage"
      useRefreshTokensFallback={true}
    >
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </Auth0Provider>
  </React.StrictMode>
);
