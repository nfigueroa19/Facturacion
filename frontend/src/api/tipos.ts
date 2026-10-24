// Forma de los DTOs de Ktor (`src/main/kotlin/.../api`). Los nombres de estados y tipos son los del dominio.

export type EstadoPrefactura =
  | "EN_REVISION"
  | "PENDIENTE"
  | "APROBADO"
  | "INGRESO_ANULADO"
  | "LISTO_PARA_FACTURAR"
  | "FACTURADO";

export const ESTADOS: readonly EstadoPrefactura[] = [
  "EN_REVISION",
  "PENDIENTE",
  "APROBADO",
  "LISTO_PARA_FACTURAR",
  "FACTURADO",
  "INGRESO_ANULADO",
];

export type TipoSoporte = "AUTORIZACION" | "ORDEN_MEDICA";
export const TIPOS_SOPORTE: readonly TipoSoporte[] = ["AUTORIZACION", "ORDEN_MEDICA"];

export type TipoCampo = "TEXTO" | "FECHA" | "CORREO" | "TELEFONO";
export type ErrorCampo = "FALTA" | "FORMATO" | "LARGO";
export type MotivoRechazoPdf = "VACIO" | "EXCEDE_TAMANO" | "NO_ES_PDF";
export type ItemRevision = "DATOS" | "AUTORIZACION" | "ORDEN_MEDICA" | "COPAGO";

export interface Cita {
  readonly fecha: string;
  readonly medico: string;
  readonly especialidad: string;
  readonly regimen: Regimen;
  readonly copagoPesos: number | null;
}

export type Regimen = "CONTRIBUTIVO" | "SUBSIDIADO";

export interface Campo {
  readonly clave: string;
  readonly etiqueta: string;
  readonly tipo: TipoCampo;
  readonly obligatorio: boolean;
  readonly ayuda: string | null;
}

export interface Pago {
  readonly referencia: string;
  readonly valorPesos: number;
  readonly fecha: string;
}

/** Parámetros firmados por el backend para abrir el widget de Wompi; el secreto de integridad nunca llega aquí. */
export interface CheckoutPago {
  readonly llavePublica: string;
  readonly referencia: string;
  readonly montoCentavos: number;
  readonly moneda: string;
  readonly firma: string;
}

/** SIMULADA mientras el backend no tenga las llaves de Wompi; CHECKOUT abre el widget de Wompi sandbox. */
export type IntencionPago = { readonly pasarela: "SIMULADA" } | { readonly pasarela: "CHECKOUT"; readonly checkout: CheckoutPago };

export interface Novedad {
  readonly descripcion: string;
  readonly fecha: string;
}

export interface SolicitudPaciente {
  readonly consecutivo: string;
  readonly estado: EstadoPrefactura;
  readonly fechaEnvio: string;
  readonly datos: Readonly<Record<string, string>>;
  readonly novedades: readonly Novedad[];
}

export interface PrefacturaPaciente {
  readonly cita: Cita;
  readonly formulario: readonly Campo[];
  readonly soportes: readonly TipoSoporte[];
  readonly pago: Pago | null;
  readonly solicitud: SolicitudPaciente | null;
  readonly tamanoMaximoSoporteBytes: number;
}

export interface ResumenSolicitud {
  readonly consecutivo: string;
  readonly estado: EstadoPrefactura;
  readonly fechaEnvio: string;
  readonly identificacion: string;
  readonly cita: Cita;
  readonly soportes: readonly TipoSoporte[];
  readonly pago: Pago | null;
}

/** Registro del facturador: número de Dinámica u observación, quién y cuándo. */
export interface Registro {
  readonly valor: string;
  readonly facturador: string;
  readonly fecha: string;
}

/** Lo que pasa desde el día de la cita: ingreso, intentos fallidos de facturación, factura o anulación. */
export interface Atencion {
  readonly ingreso: Registro | null;
  readonly intentosFallidos: readonly Registro[];
  readonly factura: Registro | null;
  readonly anulacion: Registro | null;
}

export interface DetalleSolicitud {
  readonly resumen: ResumenSolicitud;
  readonly datos: readonly { readonly clave: string; readonly etiqueta: string; readonly valor: string }[];
  readonly novedades: readonly (Novedad & { readonly facturador: string })[];
  readonly validacion: { readonly facturador: string; readonly fecha: string } | null;
  readonly checklist: readonly ItemRevision[];
  readonly atencion: Atencion;
}

export type MotivoRechazoAtencion = "ESTADO_NO_PERMITE" | "SIN_INGRESO" | "INGRESO_YA_REGISTRADO" | "FUERA_DEL_DIA_DE_LA_CITA";
export type MotivoRechazoPago = "RECHAZADA" | "FONDOS_INSUFICIENTES" | "ERROR_PASARELA" | "MEDIO_INVALIDO";

export type ErrorNuevaCita = "IDENTIFICACION" | "CELULAR" | "FECHA" | "MEDICO" | "ESPECIALIDAD" | "COPAGO";

export interface NuevaCita {
  readonly identificacion: string;
  readonly celular: string;
  readonly fecha: string;
  readonly medico: string;
  readonly especialidad: string;
  readonly regimen: Regimen;
  readonly copagoPesos: number | null;
}

export type EstadoEnvio = "ENVIADO" | "SIMULADO" | "FALLIDO";

export interface Comunicacion {
  readonly cita: string;
  readonly motivo: "CITA_ASIGNADA" | "SOLICITUD_RADICADA" | "NOVEDAD_REGISTRADA";
  readonly destino: string;
  readonly texto: string;
  readonly estado: EstadoEnvio;
  readonly canal: string;
  readonly fecha: string;
}

export interface CitaProgramada {
  readonly identificacion: string;
  readonly cita: Cita;
  readonly codigoAcceso: string;
  readonly sms: Comunicacion | null;
}

export type MotivoVentanilla = "SIN_CITA_HOY" | "SIN_RADICAR" | "EN_REVISION" | "CON_NOVEDADES" | "INGRESO_ANULADO";

export interface Turno {
  readonly turno: string;
  readonly destino: "PRECONSULTA" | "FACTURACION";
  readonly motivo: MotivoVentanilla | null;
  readonly cita: Cita | null;
  readonly consecutivo: string | null;
  readonly estado: EstadoPrefactura | null;
  readonly novedades: readonly Novedad[];
}

/** El paciente solo cambia su información antes de radicar o cuando la solicitud tiene novedades. */
export function admiteCambiosDelPaciente(solicitud: SolicitudPaciente | null): boolean {
  return solicitud === null || solicitud.estado === "PENDIENTE";
}

/** Hasta dónde avanzó la solicitud de un escenario de prueba al restablecerlo. */
export type EtapaEscenario =
  | "SIN_RADICAR"
  | "EN_REVISION"
  | "PENDIENTE"
  | "APROBADO"
  | "APROBADO_CON_INGRESO"
  | "LISTO_PARA_FACTURAR"
  | "FACTURADO"
  | "INGRESO_ANULADO";

export interface UsuarioPrueba {
  readonly usuario: string;
  readonly clave: string;
  readonly nombre: string;
  readonly modulo: string;
  readonly uso: string;
}

export interface EscenarioPaciente {
  readonly escenario: string;
  readonly identificacion: string;
  readonly codigoAcceso: string;
  readonly diasDesdeHoy: number;
  readonly etapa: EtapaEscenario;
  readonly pasos: readonly string[];
}

export interface PacienteCallCenter {
  readonly identificacion: string;
  readonly celular: string;
  readonly pasos: readonly string[];
}

/** Catálogo ficticio de `config/usuarios-prueba.json`. Solo existe mientras el backend tenga activos los escenarios. */
export interface CatalogoPruebas {
  readonly personal: readonly UsuarioPrueba[];
  readonly pacientes: readonly EscenarioPaciente[];
  readonly callcenter: readonly PacienteCallCenter[];
}
