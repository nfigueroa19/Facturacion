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
  readonly regimen: "CONTRIBUTIVO" | "SUBSIDIADO";
  readonly copagoPesos: number | null;
}

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

export interface DetalleSolicitud {
  readonly resumen: ResumenSolicitud;
  readonly datos: readonly { readonly clave: string; readonly etiqueta: string; readonly valor: string }[];
  readonly novedades: readonly (Novedad & { readonly facturador: string })[];
  readonly validacion: { readonly facturador: string; readonly fecha: string } | null;
  readonly checklist: readonly ItemRevision[];
}

/** El paciente solo cambia su información antes de radicar o cuando la solicitud tiene novedades. */
export function admiteCambiosDelPaciente(solicitud: SolicitudPaciente | null): boolean {
  return solicitud === null || solicitud.estado === "PENDIENTE";
}
