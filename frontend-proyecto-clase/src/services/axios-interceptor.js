import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8085'
});

export const setupInterceptors = (getToken) => {
  axiosInstance.interceptors.request.use(
    async (config) => {
      const token = await getToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );
  
  return axiosInstance;
};

export default axiosInstance;