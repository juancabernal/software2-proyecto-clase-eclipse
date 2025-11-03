import api from './axios-interceptor';



// --------------------- FUNCIONES DE AUTENTICACIÓN --------------------- //

/**
 * Realiza la autenticación a través de Auth0
 * @param {string} token - Token de Auth0
 * @returns {Promise<Object>}
 */
export const authenticateWithAuth0 = async (token) => {
  try {
    const response = await api.post('/auth/login', { token });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || 'Error en la autenticación');
  }
};

/**
 * Simula registro de un usuario nuevo.
 * @param {Object} userData
 * @returns {Promise<Object>}
 */
export const registerUser = async (userData) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ ...userData, id: Math.floor(Math.random() * 1000) });
    }, 600);
  });
};

// --------------------- FUNCIONES DE USUARIOS --------------------- //

/**
 * Simula una consulta de usuarios en una base de datos relacional.
 * Retorna datos en lotes de 5.
 * @param {number} page
 * @param {number} limit
 * @returns {Promise<Object>}
 */
export const getUsers = async (page = 1, limit = 5) => {
  const allUsers = [
    { id: 1, nombre: "Juan Pérez", email: "juan@example.com", rol: "admin" },
    { id: 2, nombre: "María López", email: "maria@example.com", rol: "user" },
    { id: 3, nombre: "Carlos Ramírez", email: "carlos@example.com", rol: "user" },
    { id: 4, nombre: "Ana Gómez", email: "ana@example.com", rol: "admin" },
    { id: 5, nombre: "Lucía Méndez", email: "lucia@example.com", rol: "user" },
    { id: 6, nombre: "Pedro Ortiz", email: "pedro@example.com", rol: "user" },
    { id: 7, nombre: "Laura García", email: "laura@example.com", rol: "admin" },
    { id: 8, nombre: "Juan Pérez", email: "juan@example.com", rol: "admin" },
    { id: 9, nombre: "María López", email: "maria@example.com", rol: "user" },
    { id: 10, nombre: "Carlos Ramírez", email: "carlos@example.com", rol: "user" },
    { id: 11, nombre: "Ana Gómez", email: "ana@example.com", rol: "admin" },
    { id: 12, nombre: "Lucía Méndez", email: "lucia@example.com", rol: "user" },
    { id: 13, nombre: "Pedro Ortiz", email: "pedro@example.com", rol: "user" },
    { id: 14, nombre: "Laura García", email: "laura@example.com", rol: "admin" },
    { id: 15, nombre: "Juan Pérez", email: "juan@example.com", rol: "admin" },
    { id: 16, nombre: "María López", email: "maria@example.com", rol: "user" },
    { id: 17, nombre: "Carlos Ramírez", email: "carlos@example.com", rol: "user" },
    { id: 18, nombre: "Ana Gómez", email: "ana@example.com", rol: "admin" },
    { id: 19, nombre: "Lucía Méndez", email: "lucia@example.com", rol: "user" },
    { id: 20, nombre: "Pedro Ortiz", email: "pedro@example.com", rol: "user" },
    { id: 21, nombre: "Laura García", email: "laura@example.com", rol: "admin" },
  ];

  return new Promise((resolve) => {
    setTimeout(() => {
      const startIndex = (page - 1) * limit;
      const endIndex = page * limit;
      const paginated = allUsers.slice(startIndex, endIndex);
      resolve({
        data: paginated,
        total: allUsers.length,
        totalPages: Math.ceil(allUsers.length / limit),
      });
    }, 500);
  });
};

const sanitizeUserId = (userId) => {
  if (userId === undefined || userId === null) {
    return '';
  }
  return String(userId).trim();
};

const requestConfirmation = async ({ userId, type, failureMessage }) => {
  const trimmedId = sanitizeUserId(userId);
  if (!trimmedId) {
    throw new Error('Es necesario proporcionar el identificador del usuario.');
  }

  const encodedId = encodeURIComponent(trimmedId);
  const endpoints = [
    `/api/admin/users/${encodedId}/confirmations/${type}`,
    `/uco-challenge/api/v1/users/${encodedId}/confirmations/${type}`,
  ];

  let lastError = null;

  for (let index = 0; index < endpoints.length; index += 1) {
    try {
      const response = await api.post(endpoints[index]);
      return response.data;
    } catch (error) {
      lastError = error;
      const status = error?.response?.status;
      const shouldFallback = index === 0 && status === 404;
      if (!shouldFallback) {
        break;
      }
    }
  }

  const message =
    lastError?.response?.data?.message ||
    lastError?.response?.data?.error ||
    failureMessage;
  const enhancedError = new Error(message);
  enhancedError.originalError = lastError;
  throw enhancedError;
};

export const requestEmailConfirmation = async (userId) =>
  requestConfirmation({
    userId,
    type: 'email',
    failureMessage: 'No fue posible solicitar la validación del correo electrónico.',
  });

export const requestMobileConfirmation = async (userId) =>
  requestConfirmation({
    userId,
    type: 'mobile',
    failureMessage: 'No fue posible solicitar la validación del teléfono móvil.',
  });
