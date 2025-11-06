import axios from 'axios'
import { parseApiError } from '../utils/parseApiError'

function trimTrailingSlash(url: string) {
  return url.endsWith('/') ? url.slice(0, -1) : url
}

const DEFAULT_BASE_URL = '/api/uco-challenge/api/v1'
const resolvedBaseUrl = trimTrailingSlash(import.meta.env.VITE_API_BASE_URL ?? DEFAULT_BASE_URL)

export const api = axios.create({
  baseURL: resolvedBaseUrl,
  headers: { 'Content-Type': 'application/json' },
})

export const apiClient = api

api.interceptors.response.use(
  (response) => response,
  (error) => {
    ;(error as any).__niceMessage = parseApiError(error)
    return Promise.reject(error)
  }
)
