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

// Debug: mostrar variables de entorno (sin exponer secrets)
console.log('VITE_AUTH0_DOMAIN=', import.meta.env.VITE_AUTH0_DOMAIN);
console.log('VITE_AUTH0_CLIENT_ID=', import.meta.env.VITE_AUTH0_CLIENT_ID);

root.render(
  <React.StrictMode>
    <Auth0Provider
      domain={import.meta.env.VITE_AUTH0_DOMAIN}
      clientId={import.meta.env.VITE_AUTH0_CLIENT_ID}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience: `https://${import.meta.env.VITE_AUTH0_DOMAIN}/api/v2/`,
        scope: 'openid profile email read write'
      }}
      useRefreshTokens={true}
      cacheLocation="localstorage"
    >
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </Auth0Provider>
  </React.StrictMode>
);
