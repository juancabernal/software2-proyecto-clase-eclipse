import { useAuth0 } from "@auth0/auth0-react";

const Profile = () => {
  const { user, isAuthenticated, isLoading } = useAuth0();

  if (isLoading) {
    return (
      <div className="min-h-screen grid place-items-center bg-[#0f0f12] text-gray-200">
        <div className="animate-pulse rounded-xl bg-[#141418] px-6 py-4">
          Cargando perfil...
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return (
      <div className="min-h-screen grid place-items-center bg-[#0f0f12] text-gray-200">
        <div className="rounded-xl bg-[#141418] px-6 py-4 text-red-400">
          No se pudo cargar el perfil
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0f0f12] text-gray-100">
      <div className="mx-auto max-w-3xl px-4 py-12">
        <div className="rounded-2xl border border-gray-800 bg-[#141418] p-8">
          {/* Header con foto y nombre */}
          <div className="flex items-center space-x-6">
            {user.picture && (
              <img
                src={user.picture}
                alt={user.name || 'Foto de perfil'}
                className="h-24 w-24 rounded-full border-4 border-indigo-500 shadow-xl"
              />
            )}
            <div>
              <h1 className="text-3xl font-bold text-white">{user.name}</h1>
              <p className="mt-1 text-lg text-gray-400">{user.email}</p>
            </div>
          </div>

          {/* Detalles del perfil */}
          <div className="mt-8 grid gap-6 sm:grid-cols-2">
            <div className="rounded-xl border border-gray-700 bg-[#1a1a1f] p-5">
              <h3 className="text-sm font-medium text-gray-400">ID de Usuario</h3>
              <p className="mt-2 font-mono text-sm text-white">{user.sub}</p>
            </div>

            <div className="rounded-xl border border-gray-700 bg-[#1a1a1f] p-5">
              <h3 className="text-sm font-medium text-gray-400">Rol</h3>
              <p className="mt-2 text-sm text-white capitalize">{user['uco_role'] || 'Usuario'}</p>
            </div>

            <div className="rounded-xl border border-gray-700 bg-[#1a1a1f] p-5">
              <h3 className="text-sm font-medium text-gray-400">Email Verificado</h3>
              <p className="mt-2 text-sm text-white">
                {user.email_verified ? (
                  <span className="inline-flex items-center rounded-full bg-green-500/10 px-3 py-1 text-sm font-medium text-green-400">
                    Verificado
                  </span>
                ) : (
                  <span className="inline-flex items-center rounded-full bg-yellow-500/10 px-3 py-1 text-sm font-medium text-yellow-400">
                    Pendiente
                  </span>
                )}
              </p>
            </div>

            <div className="rounded-xl border border-gray-700 bg-[#1a1a1f] p-5">
              <h3 className="text-sm font-medium text-gray-400">Último Acceso</h3>
              <p className="mt-2 text-sm text-white">
                {new Date(user.updated_at).toLocaleString()}
              </p>
            </div>
          </div>

          {/* Metadata adicional si existe */}
          {user.user_metadata && (
            <div className="mt-8 rounded-xl border border-gray-700 bg-[#1a1a1f] p-5">
              <h3 className="text-sm font-medium text-gray-400">Información Adicional</h3>
              <pre className="mt-2 overflow-x-auto text-sm text-white">
                {JSON.stringify(user.user_metadata, null, 2)}
              </pre>
            </div>
          )}
        </div>
      </div>
    </div>
  );

export default Profile;
