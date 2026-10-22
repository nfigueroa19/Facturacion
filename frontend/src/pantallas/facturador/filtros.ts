import type { EstadoPrefactura, ResumenSolicitud } from "../../api/tipos";

export interface Filtros {
  /** null muestra todos los estados. El estado se filtra en el backend; texto y fecha, en el navegador. */
  readonly estado: EstadoPrefactura | null;
  readonly texto: string;
  readonly fechaCita: string;
}

export const FILTROS_INICIALES: Filtros = { estado: "EN_REVISION", texto: "", fechaCita: "" };

export function filtrar(solicitudes: readonly ResumenSolicitud[], { texto, fechaCita }: Filtros): readonly ResumenSolicitud[] {
  const buscado = texto.trim().toUpperCase();
  return solicitudes.filter(
    (solicitud) =>
      (buscado === "" || solicitud.identificacion.includes(buscado) || solicitud.consecutivo.toUpperCase().includes(buscado)) &&
      (fechaCita === "" || solicitud.cita.fecha === fechaCita),
  );
}
