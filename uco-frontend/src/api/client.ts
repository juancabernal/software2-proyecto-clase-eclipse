import axios, { type AxiosInstance } from 'axios'
import { parseApiError } from '../utils/parseApiError'

function trimTrailingSlash(url: string) {
  return url.endsWith('/') ? url.slice(0, -1) : url
}

const DEFAULT_PUBLIC_BASE_URL = '/api'
const resolvedPublicBaseUrl = trimTrailingSlash(
  import.meta.env.VITE_API_BASE_URL ?? DEFAULT_PUBLIC_BASE_URL,
)

const resolvedAdminBaseUrl = trimTrailingSlash(
  import.meta.env.VITE_API_ADMIN_BASE_URL ?? `${resolvedPublicBaseUrl}/admin`,
)

const createHttpClient = (baseURL: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    headers: { 'Content-Type': 'application/json' },
  })

  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      ;(error as any).__niceMessage = parseApiError(error)
      return Promise.reject(error)
    },
  )

  return instance
}

export const api = createHttpClient(resolvedPublicBaseUrl)
export const adminApi = createHttpClient(resolvedAdminBaseUrl)

// Mantener exportación histórica para módulos que aún la consumen.
export const apiClient = adminApi
