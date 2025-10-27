import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8086'
});

let interceptorsConfigured = false;

export const setupInterceptors = (getToken) => {
  if (!interceptorsConfigured) {
    axiosInstance.interceptors.request.use(
      async (config) => {
        if (typeof getToken === 'function') {
          try {
            const token = await getToken();
            if (token) {
              config.headers.Authorization = `Bearer ${token}`;
            }
          } catch (err) {
            console.warn('No fue posible adjuntar el token de Auth0 a la petición:', err);
          }
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    interceptorsConfigured = true;
  }

  return axiosInstance;
};

export default axiosInstance;