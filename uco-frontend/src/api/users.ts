import { adminApi } from './client'

// Payload que viene del formulario actual del front
export type RegisterUserPayloadUI = {
  documentTypeId?: string
  documentTypeName?: string
  documentNumber: string
  firstName: string
  middleName?: string
  lastName: string
  secondLastName?: string
  email?: string
  mobile?: string
  countryId: string
  departmentId: string
  cityId: string
}

// DTO exacto que el backend espera
type RegisterUserInputDTO = {
  idTypeId?: string
  idTypeName?: string
  idNumber: string
  firstName: string
  middleName?: string
  lastName: string
  secondLastName?: string
  email?: string
  mobile?: string
  countryId: string
  departmentId: string
  cityId: string
}

export type RegisterUserResponse = {
  id: string
  idTypeId: string
  idNumber: string
  firstName: string
  middleName?: string | null
  lastName: string
  secondLastName?: string | null
  email?: string | null
  mobile?: string | null
  cityId: string
}

export type UserSummary = {
  id: string
  firstName: string
  lastName?: string | null
  email: string
  mobileNumber?: string | null
  emailConfirmed?: boolean | null
  mobileNumberConfirmed?: boolean | null
}

export type UsersPage = {
  users: UserSummary[]
  page: number
  size: number
  totalElements: number
}

type CreateUserOptions = {
  idempotencyKey?: string
}

const ADMIN_USERS_ENDPOINT = '/users'

const requiresBody = (method: string) => ['post', 'put', 'patch'].includes(method.toLowerCase())

const generateIdempotencyKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

// 🔁 Mapea las claves del UI al formato que el backend necesita
function mapToRegisterUserDTO(ui: RegisterUserPayloadUI): RegisterUserInputDTO {
  return {
    idTypeId: ui.documentTypeId,
    idTypeName: ui.documentTypeName,
    idNumber: ui.documentNumber,
    firstName: ui.firstName,
    middleName: ui.middleName || undefined,
    lastName: ui.lastName,
    secondLastName: ui.secondLastName || undefined,
    email: ui.email || undefined,
    mobile: ui.mobile || undefined,
    countryId: ui.countryId,
    departmentId: ui.departmentId,
    cityId: ui.cityId,
  }
}

// ✅ Llama al endpoint correcto /api/admin/users y maneja errores claramente
export async function createUser(
  formPayload: RegisterUserPayloadUI,
  options?: CreateUserOptions,
): Promise<RegisterUserResponse> {
  const payload = mapToRegisterUserDTO(formPayload)
  const idempotencyKey = options?.idempotencyKey ?? generateIdempotencyKey()
  const headers = { 'Idempotency-Key': idempotencyKey }
  const logLabel = `[api.createUser] POST ${ADMIN_USERS_ENDPOINT}`

  console.groupCollapsed(`${logLabel} :: request`)
  console.debug('Headers', headers)
  console.debug('Payload', payload)
  console.groupEnd()

  const startedAt = typeof performance !== 'undefined' ? performance.now() : Date.now()

  try {
    const response = await adminApi.post<RegisterUserResponse>(ADMIN_USERS_ENDPOINT, payload, {
      headers,
    })

    const elapsed = (typeof performance !== 'undefined' ? performance.now() : Date.now()) - startedAt

    console.groupCollapsed(`${logLabel} :: response [${response.status}] (${elapsed.toFixed(1)}ms)`)
    console.debug('Response data', response.data)
    console.groupEnd()

    if (response.status !== 201) {
      const error: any = new Error('Respuesta inesperada al registrar usuario')
      error.response = response
      error.userMessage = 'El servicio no confirmó la creación del usuario.'
      error.requestContext = { idempotencyKey, payload }
      throw error
    }

    return response.data
  } catch (error: any) {
    const elapsed = (typeof performance !== 'undefined' ? performance.now() : Date.now()) - startedAt

    const status = error?.response?.status
    const err = error?.response?.data
    const apiError = err?.data ?? err

    const message =
      apiError?.userMessage ??
      apiError?.message ??
      error?.userMessage ??
      'Ocurrió un error al registrar el usuario.'

    const field = apiError?.details?.field
    const value = apiError?.details?.value

    if (error && typeof error === 'object') {
      error.userMessage = message
      error.duplicateField = field
      error.duplicateValue = value
      error.requestContext = {
        idempotencyKey,
        payload,
        status,
      }
    }

    console.groupCollapsed(`${logLabel} :: error [${status ?? 'network'}] (${elapsed.toFixed(1)}ms)`)
    console.error('Request payload', payload)
    if (requiresBody(error?.config?.method ?? '')) {
      console.error('Axios config body', error?.config?.data)
    }
    console.error('Response data', apiError ?? error?.response?.data ?? error)
    console.groupEnd()

    console.error('Error creando usuario:', status, apiError)
    throw error
  }
}

// Para listar usuarios (ya estaba bien)
export async function getUsers(page = 0, size = 10): Promise<UsersPage> {
  const { data } = await adminApi.get<UsersPage>(ADMIN_USERS_ENDPOINT, { params: { page, size } })
  return data
}
