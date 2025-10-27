import React, { createContext, useContext, useEffect, useState } from 'react';
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
import Navbar from './components/Navbar';

/**
 * AuthContext (frontend-only, simulado)
 * - Mantiene estado de "admin" conectado
 * - Provee funciones de login/logout para que los componentes del frontend las usen
 *
 * Nota: aquí NO se implementa autenticación real con Google/GitHub — sólo mapeos/placeholder.
 */
const AuthContext = createContext({
  user: null,
  loginWithCredentials: async () => {},
  loginWithOAuth: async () => {},
  logout: () => {}
});

export const useAuth = () => useContext(AuthContext);

/* Componente que protege rutas para administradores */
function PrivateRoute({ children }) {
  const { user } = useAuth();
  if (!user) {
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
    logout: auth0Logout,
    getAccessTokenSilently,
    error: auth0Error
  } = useAuth0();

  // Log cualquier error de autenticación
  useEffect(() => {
    if (auth0Error) {
      console.error('Auth0 error:', auth0Error);
    }
  }, [auth0Error]);
  const location = useLocation();
  const ADMIN_CLAIM = import.meta.env.VITE_AUTH0_ADMIN_CLAIM || '';

  // Helper: intenta determinar si el usuario de Auth0 tiene el rol 'admin'.
  const isAdminAuth0 = (au) => {
    if (!au) return false;

    // Propiedades comunes donde se pueden encontrar roles/permissions
    const possible = [
      'roles',
      'role',
      'permissions',
      'http://schemas.microsoft.com/ws/2008/06/identity/claims/role',
    ];

    for (const k of possible) {
      if (Array.isArray(au[k]) && au[k].includes('admin')) return true;
      if (typeof au[k] === 'string' && au[k] === 'admin') return true;
    }

    // Si el proyecto definió una claim específica en .env, usarla (soporta rutas con puntos)
    if (ADMIN_CLAIM) {
      const getByPath = (obj, path) => {
        if (!obj) return undefined;
        if (path.includes('.')) {
          return path.split('.').reduce((acc, key) => (acc ? acc[key] : undefined), obj);
        }
        return obj[path];
      };

      const claimVal = getByPath(au, ADMIN_CLAIM) || au[ADMIN_CLAIM];
      if (Array.isArray(claimVal) && claimVal.includes('admin')) return true;
      if (typeof claimVal === 'string' && claimVal === 'admin') return true;
    }

    // Buscar en claims personalizados: cualquier claim que sea array y contenga 'admin'
    for (const k of Object.keys(au)) {
      const val = au[k];
      if (Array.isArray(val) && val.includes('admin')) return true;
    }

    // app_metadata.roles es otra ubicación común
    if (au.app_metadata && Array.isArray(au.app_metadata.roles) && au.app_metadata.roles.includes('admin')) return true;

    return false;
  };

  // Sincroniza el usuario de Auth0 con el AuthContext local para no cambiar el resto de la app
  useEffect(() => {
    const checkUserAndRoles = async () => {
      if (isAuthenticated && auth0User) {
        try {
          console.log('=== Debug Auth0 ===');
          console.log('User profile:', auth0User);
          
          // Obtener token con scope para roles
          const token = await getAccessTokenSilently({
            authorizationParams: {
              audience: `https://${import.meta.env.VITE_AUTH0_DOMAIN}/api/v2/`,
              scope: 'read:roles'
            }
          });

          // Llamar a la API de gestión de Auth0 para obtener roles
          const response = await fetch(`https://${import.meta.env.VITE_AUTH0_DOMAIN}/api/v2/users/${auth0User.sub}/roles`, {
            headers: {
              Authorization: `Bearer ${token}`
            }
          });

          if (response.ok) {
            const roles = await response.json();
            console.log('Roles del usuario:', roles);
            
            // Si el usuario tiene rol admin, sincronizar en el estado local
            if (roles.some(role => role.name === 'admin')) {
              const adminUser = {
                id: auth0User.sub,
                name: auth0User.name || auth0User.nickname || 'Usuario',
                email: auth0User.email,
                role: 'admin',
                provider: auth0User.sub.split('|')[0]
              };
              setUser(adminUser);
              
              // Si venimos de login, redirigir al dashboard
              if (location.pathname === '/login') {
                navigate('/dashboard', { replace: true });
              }
            } else {
              console.log('Usuario no tiene rol admin - forzando logout');
              await auth0Logout();
            }
          }
        } catch (e) {
          console.error('Error al verificar roles:', e);
        }
      }
      console.log('Auth0 isAuthenticated=', isAuthenticated);
    };
    
    checkUserAndRoles();

    // Cuando Auth0 indica autenticación, decidir si es admin
    if (isAuthenticated && auth0User) {
      const admin = isAdminAuth0(auth0User);

      if (!admin) {
        // Si no es admin, forzamos logout en Auth0 y no permitimos el acceso
        try {
          if (auth0Logout) auth0Logout({ returnTo: window.location.origin });
        } catch (e) {
          console.warn('Error al ejecutar auth0Logout para usuario no admin:', e);
        }
        setUser(null);
        // Mantener al usuario en /login (o redirigir ahí)
        if (location.pathname !== '/login') navigate('/login', { replace: true });
        // Feedback mínimo sin tocar diseño
        setTimeout(() => alert('Acceso denegado: tu cuenta no tiene el rol de administrador.'), 300);
        return;
      }

      const synced = {
        id: auth0User.sub,
        name: auth0User.name || auth0User.nickname || 'Usuario',
        email: auth0User.email,
        role: 'admin',
        provider: auth0User.sub && auth0User.sub.split('|')[0],
      };
      setUser(synced);

      // Si venimos del flujo de login (o estamos en /login), redirigimos al dashboard
      if (location.pathname === '/login' || location.pathname === '/') {
        navigate('/dashboard', { replace: true });
      }
    } else if (!isAuthenticated) {
      setUser(null);
    }

    // Configurar interceptor de Axios cuando el usuario está autenticado
    if (isAuthenticated) {
      setupInterceptors();
    }
  }, [isAuthenticated, auth0User]);

  /**
   * Simula login con usuario/password (frontend-only).
   * Recibe un objeto { email, name } del LoginForm y setea el usuario.
   * Después de "loguear", navega al dashboard.
   */
  const loginWithCredentials = async ({ email, name }) => {
    // Validaciones / checks pueden hacerse en el LoginForm; aquí solo simulamos éxito.
    const adminUser = {
      id: 'admin-1',
      name: name || 'Administrador',
      email,
      role: 'admin',
    };
    setUser(adminUser);
    navigate('/dashboard', { replace: true });
  };

  /**
   * Simula login vía OAuth (Google / GitHub).
   * `provider` es 'google' | 'github' (mapeo visual en OAuthButtons).
   */
  const loginWithOAuth = async (provider) => {
    // placeholder: aquí llamarías a tu flujo OAuth real
    const oauthUser = {
      id: `oauth-${provider}-1`,
      name: provider === 'google' ? 'Admin Google' : 'Admin GitHub',
      email: provider === 'google' ? 'admin@google.com' : 'admin@github.com',
      provider,
      role: 'admin',
    };

    // simulación suave de animación/retardo
    await new Promise((res) => setTimeout(res, 350));
    setUser(oauthUser);
    navigate('/dashboard', { replace: true });
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
    loginWithOAuth,
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
