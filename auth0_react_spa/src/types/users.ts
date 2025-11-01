export type User = {
  id: string;
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  correo: string;
  telefono?: string;
  ciudad?: string;
  estado?: string;
  pais?: string;
  // agrega lo que necesites (fechaAlta, activo, etc.)
};
