import type { ResumenSolicitud } from "../../api/tipos";
import { TIPOS_SOPORTE } from "../../api/tipos";
import { EstadoSolicitud } from "../../componentes/EstadoSolicitud";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, fechaCorta, fechaHora } from "../../formato";

const COLUMNAS = ["radicado", "identificacion", "fecha-cita", "medico", "soportes", "copago", "enviada", "estado"] as const;

function estadoCopago(solicitud: ResumenSolicitud): string {
  if (solicitud.cita.copagoPesos === null) return "copago.columna.no-aplica";
  return solicitud.pago === null ? "copago.columna.ventanilla" : "copago.columna.pagado";
}

export interface PropsTabla {
  readonly solicitudes: readonly ResumenSolicitud[];
  readonly seleccionada: string | null;
  readonly texto: Texto;
  readonly onSeleccionar: (consecutivo: string) => void;
}

/** Filas de 40 px, sin cebra ni banda de color en el encabezado (DESIGN.md 5.3). */
export function TablaSolicitudes({ solicitudes, seleccionada, texto, onSeleccionar }: PropsTabla) {
  return (
    <div className="overflow-x-auto rounded-[6px] border border-border">
      <table className="w-full border-collapse text-left text-sm">
        <caption className="sr-only">{texto("solicitudes.titulo")}</caption>
        <thead>
          <tr className="border-b border-border">
            {COLUMNAS.map((columna) => (
              <th key={columna} scope="col" className={`h-10 whitespace-nowrap px-3 font-medium text-ink-muted ${columna === "soportes" ? "text-right" : ""}`}>
                {texto(`columna.${columna}`)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {solicitudes.map((solicitud) => {
            const activa = solicitud.consecutivo === seleccionada;
            return (
              <tr key={solicitud.consecutivo} className={`h-10 border-b border-border last:border-b-0 ${activa ? "bg-surface-secondary" : "hover:bg-surface-secondary"}`}>
                <th scope="row" className="px-3 font-medium">
                  <button
                    type="button"
                    aria-current={activa ? "true" : undefined}
                    onClick={() => onSeleccionar(solicitud.consecutivo)}
                    className="min-h-8 whitespace-nowrap rounded-[6px] px-1 text-action underline underline-offset-2 hover:text-action-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring"
                  >
                    {solicitud.consecutivo}
                  </button>
                </th>
                <td className="px-3 tabular-nums">{solicitud.identificacion}</td>
                <td className="whitespace-nowrap px-3 tabular-nums">{fechaCorta(solicitud.cita.fecha)}</td>
                <td className="px-3">{solicitud.cita.medico}</td>
                <td className="whitespace-nowrap px-3 text-right tabular-nums">
                  {conValores(texto("solicitudes.soportes-cargados"), { cargados: solicitud.soportes.length, total: TIPOS_SOPORTE.length })}
                </td>
                <td className="whitespace-nowrap px-3">{texto(estadoCopago(solicitud))}</td>
                <td className="whitespace-nowrap px-3 tabular-nums">{fechaHora(solicitud.fechaEnvio)}</td>
                <td className="px-3">
                  <EstadoSolicitud estado={solicitud.estado} texto={texto} />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
