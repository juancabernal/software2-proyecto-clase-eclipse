import { api } from './client'

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

// ✅ Llama al endpoint correcto /users y maneja errores claramente
export async function createUser(formPayload: RegisterUserPayloadUI) {
  const payload = mapToRegisterUserDTO(formPayload)
  try {
    const { data } = await api.post('/users', payload)
    return data
  } catch (error: any) {
    const err = error?.response?.data
    const apiError = err?.data ?? err

    const message =
      apiError?.userMessage ??
      apiError?.message ??
      'Ocurrió un error al registrar el usuario.'

    const field = apiError?.details?.field
    const value = apiError?.details?.value

    if (error && typeof error === 'object') {
      error.userMessage = message
      error.duplicateField = field
      error.duplicateValue = value
    }

    console.error('Error creando usuario:', error?.response?.status, apiError)
    throw error
  }
}

// Para listar usuarios (ya estaba bien)
export async function getUsers(page = 0, size = 10) {
  const { data } = await api.get('/users', { params: { page, size } })
  return data
}
