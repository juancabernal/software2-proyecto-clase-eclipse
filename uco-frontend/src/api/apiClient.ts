import { AxiosError } from 'axios'
import type { GetTokenSilentlyOptions } from '@auth0/auth0-react'
import { api, adminApi } from './client'

let getTokenSilentlyFn: ((opts?: GetTokenSilentlyOptions) => Promise<string>) | null = null
let interceptorsAttached = false

const clients = [api, adminApi]

export const attachTokenInterceptor = (
  getTokenSilently: (opts?: GetTokenSilentlyOptions) => Promise<string>,
) => {
  getTokenSilentlyFn = getTokenSilently

  if (interceptorsAttached) {
    return
  }

  clients.forEach((client) => {
    client.interceptors.request.use(async (config) => {
      if (getTokenSilentlyFn) {
        const token = await getTokenSilentlyFn({
          authorizationParams: {
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            scope: import.meta.env.VITE_AUTH0_SCOPE || 'openid profile email',
          },
        })
        if (token) config.headers.Authorization = `Bearer ${token}`
      }
      return config
    })

    client.interceptors.response.use(
      (res) => res,
      (err: AxiosError) => {
        const status = err.response?.status
        if (status === 401) window.location.href = '/login'
        if (status === 403) window.location.href = '/not-authorized'
        return Promise.reject(err)
      },
    )
  })

  interceptorsAttached = true
}
