import { adminApi } from './client'

const USERS_BASE = '/users'

export async function confirmUserEmail(id: string) {
  await adminApi.post(`${USERS_BASE}/${id}/confirm-email`)
}

export async function confirmUserMobile(id: string) {
  await adminApi.post(`${USERS_BASE}/${id}/confirm-mobile`)
}
