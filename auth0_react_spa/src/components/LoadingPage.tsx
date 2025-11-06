import { LoadingSpinner } from './LoadingSpinner';

export function LoadingPage() {
  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center">
      <div className="text-center">
        <LoadingSpinner size="large" className="mb-4" />
        <h2 className="text-xl font-semibold text-white">Cargando...</h2>
      </div>
    </div>
  );
}