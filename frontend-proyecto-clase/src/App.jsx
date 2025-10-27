import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuth0 } from '@auth0/auth0-react';
import { setupInterceptors } from './services/axios-interceptor';

/* Pages (existentes en tu estructura) */
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import RegisterUserPage from './pages/RegisterUserPage';
import UserSearchPage from './pages/UserSearchPage';

/* Components (se asume que existan) */

/**
 * AuthContext (frontend-only, simulado)
 * - Mantiene estado de "admin" conectado
 * - Provee funciones de login/logout para que los componentes del frontend las usen
 *
 * Nota: aquí NO se implementa autenticación real con Google/GitHub — sólo mapeos/placeholder.
 */
const AuthContext = createContext({
  user: null,
  logout: () => {},
  isAdmin: false
});

export const useAuth = () => useContext(AuthContext);

/* Componente que protege rutas para administradores */
function PrivateRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuth0();
  const { user } = useAuth();

  if (isLoading) {
    return <div>Cargando...</div>;
  }

  if (!isAuthenticated || !user?.isAdmin) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

/* Mock: usuario admin por defecto (null hasta login) */
export default function App() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();
  const {
    user: auth0User,
    isAuthenticated,
    isLoading,
    loginWithRedirect,
    logout: auth0Logout,
    getAccessTokenSilently,
    getIdTokenClaims,
    error: auth0Error
  } = useAuth0();

  // Log cualquier error de autenticación
  useEffect(() => {
    if (auth0Error) {
      console.error('Auth0 error:', auth0Error);
    }
  }, [auth0Error]);
  const location = useLocation();
  const interceptorsConfiguredRef = useRef(false);
  const ADMIN_ROLE = import.meta.env.VITE_AUTH0_ADMIN_ROLE || 'admin';
  const domainNamespace = import.meta.env.VITE_AUTH0_DOMAIN
    ? `https://${import.meta.env.VITE_AUTH0_DOMAIN}/`
    : '';
  const ADMIN_CLAIM =
    import.meta.env.VITE_AUTH0_ADMIN_CLAIM ||
    (domainNamespace ? `${domainNamespace}roles` : 'roles');
  const API_AUDIENCE = import.meta.env.VITE_AUTH0_AUDIENCE;
  const API_SCOPE = import.meta.env.VITE_AUTH0_API_SCOPE || import.meta.env.VITE_AUTH0_SCOPE;

  // Helpers relacionados con roles provenientes de Auth0
const extractRoles = (claims) => {
  const namespace = import.meta.env.VITE_AUTH0_DOMAIN
    ? `https://${import.meta.env.VITE_AUTH0_DOMAIN}/`
    : '';
  return (
    claims?.[`${namespace}roles`] ||
    claims?.roles ||
    claims?.permissions ||
    []
  );
};


  const isAdminAuth0 = (claims, profile) => {
    if (!profile) return false;
    const roles = extractRoles(claims, profile);
    return roles.includes(ADMIN_ROLE);
  };

  // Sincroniza el usuario de Auth0 con el AuthContext local para no cambiar el resto de la app
  useEffect(() => {
    if (isLoading) return; // Esperar a que Auth0 termine de cargar

    const syncSession = async () => {
      if (isAuthenticated && auth0User) {
        try {
          const accessToken = await getAccessTokenSilently({
  authorizationParams: {
    audience: API_AUDIENCE,
    scope: API_SCOPE
  }
});
const decoded = JSON.parse(atob(accessToken.split('.')[1]));
const roles = extractRoles(decoded, auth0User);
          if (!roles.includes(ADMIN_ROLE)) {
            console.log('Usuario no tiene rol admin - forzando logout');
            await auth0Logout({ returnTo: window.location.origin });
            setUser(null);
            if (location.pathname !== '/login') navigate('/login', { replace: true });
            setTimeout(() => alert('Acceso denegado: tu cuenta no tiene el rol de administrador.'), 300);
            return;
          }

          const adminUser = {
            id: auth0User.sub,
            name: auth0User.name || auth0User.nickname || 'Usuario',
            email: auth0User.email,
            role: ADMIN_ROLE,
            roles,
            provider: auth0User.sub.split('|')[0]
          };

          setUser(adminUser);

          if (location.pathname === '/login' || location.pathname === '/') {
            navigate('/dashboard', { replace: true });
          }

          if (!interceptorsConfiguredRef.current) {
            setupInterceptors(() =>
              getAccessTokenSilently({
                authorizationParams: {
                  ...(API_AUDIENCE ? { audience: API_AUDIENCE } : {}),
                  ...(API_SCOPE ? { scope: API_SCOPE } : {}),
                }
              })
            );
            interceptorsConfiguredRef.current = true;
          }
        } catch (e) {
          console.error('Error al verificar roles:', e);
        }
      } else {
        // No autenticado o error
        setUser(null);
        if (location.pathname !== '/login' && location.pathname !== '/') {
          navigate('/login', { replace: true });
        }
      }

      // Solo loguear en desarrollo
      if (import.meta.env.DEV) {
        console.log('Auth0 state:', { 
          isAuthenticated, 
          isLoading, 
          hasUser: !!auth0User 
        });
      }
    };

    syncSession();
  }, [
    auth0User,
    isAuthenticated,
    isLoading,
    getAccessTokenSilently,
    location.pathname,
    navigate,
    auth0Logout
  ]);

  /**
   * Simula login con usuario/password (frontend-only).
   * Recibe un objeto { email, name } del LoginForm y setea el usuario.
   * Después de "loguear", navega al dashboard.
   */
  const loginWithCredentials = async ({ email }) => {
    try {
      await loginWithRedirect({
        authorizationParams: {
          login_hint: email,
          ...(API_AUDIENCE ? { audience: API_AUDIENCE } : {}),
          scope: ['openid profile email', API_SCOPE].filter(Boolean).join(' ').trim() || 'openid profile email',
          redirect_uri: window.location.origin,
        },
      });
    } catch (error) {
      console.error('Error iniciando sesión con Auth0:', error);
    }
  };

  const logout = () => {
    // Si hay logout de Auth0 disponible, usarlo para limpiar sesión en Auth0
    try {
      if (auth0Logout) {
        auth0Logout({ returnTo: window.location.origin });
        // auth0Logout suele redirigir; aún así limpiamos el estado local
      }
    } catch (e) {
      console.warn('Auth0 logout no disponible o fallo:', e);
    }

    setUser(null);
    navigate('/login', { replace: true });
  };

  const authValue = {
    user,
    loginWithCredentials,
    logout,
  };

  return (
    <AuthContext.Provider value={authValue}>
      <div className="app-shell">
        {/* Topbar minimal */}
        <header className="topbar card">
          <div className="brand">
            <div className="logo" aria-hidden>
              {/* Letra/icono simple — puedes reemplazar por un SVG */}
              
            </div>
            <div>
              <div className="title">Admin Panel</div>
              <div className="text-xs muted">UCO Challenge</div>
            </div>
          </div>

          <div className="header-actions">
            {/* Si hay usuario, mostrar nombre y botón logout */}
            {user ? (
              <>
                <div className="badge">{user.name}</div>
                <button className="btn btn-ghost" onClick={logout} title="Cerrar sesión">
                  Cerrar sesión
                </button>
              </>
            ) : (
              <div className="badge">No autorizado</div>
            )}
          </div>
        </header>

        <main className="container page">
          <motion.div
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.45 }}
          >
            {/* Area principal con rutas (SE ELIMINA SIDEBAR/ASIDE completamente) */}
            <section style={{ minHeight: 520 }} className="card">
              <Routes>
                 <Route
                   path="/"
                   element={<Navigate to="/login" replace />}
                 />
 
                 <Route
                   path="/login"
                   element={<LoginPage />}
                 />
 
                 {/* Rutas protegidas */}
                 <Route
                   path="/dashboard"
                   element={
                     <PrivateRoute>
                       <DashboardPage />
                     </PrivateRoute>
                   }
                 />
 
                 <Route
                   path="/register-user"
                   element={
                     <PrivateRoute>
                       <RegisterUserPage />
                     </PrivateRoute>
                   }
                 />
 
                 <Route
                   path="/users"
                   element={
                     <PrivateRoute>
                       <UserSearchPage />
                     </PrivateRoute>
                   }
                 />
 
                 {/* fallback */}
                 <Route path="*" element={<div className="empty-state"><h3 className="h1">Página no encontrada</h3><p className="muted">Revisa la URL o vuelve al panel.</p></div>} />
               </Routes>
             </section>
           </motion.div>
        </main>
      </div>
    </AuthContext.Provider>
  );
}
