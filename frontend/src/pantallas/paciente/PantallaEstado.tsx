import { Button } from "@heroui/react";
import type { PrefacturaPaciente, SolicitudPaciente } from "../../api/tipos";
import { EstadoSolicitud } from "../../componentes/EstadoSolicitud";
import type { Texto } from "../../contenido/ContenidoContexto";
import { fechaHora } from "../../formato";
import { ResumenCita } from "./ResumenCita";

interface Props {
  readonly prefactura: PrefacturaPaciente;
  readonly solicitud: SolicitudPaciente;
  readonly texto: Texto;
  /** Solo existe cuando la solicitud tiene novedades por corregir. */
  readonly onCorregir: (() => void) | null;
  readonly onSalir: () => void;
}

export function PantallaEstado({ prefactura, solicitud, texto, onCorregir, onSalir }: Props) {
  const conNovedades = solicitud.estado === "PENDIENTE" && solicitud.novedades.length > 0;
  return (
    <section aria-labelledby="titulo-estado" className="aparicion-estado flex flex-col gap-6">
      <h1 id="titulo-estado" className="text-2xl font-bold text-brand-navy">
        {texto("estado.titulo")}
      </h1>
      <div className="flex flex-col gap-3 rounded-[6px] border border-border bg-surface-secondary p-4">
        <p className="text-sm font-medium text-ink-muted">{texto("estado.consecutivo")}</p>
        <p className="select-all text-3xl font-bold tabular-nums text-brand-navy">{solicitud.consecutivo}</p>
        <div>
          <EstadoSolicitud estado={solicitud.estado} texto={texto} />
        </div>
      </div>
      <p className="text-base text-ink">{texto(`estado.${solicitud.estado}.paciente`)}</p>
      {conNovedades && (
        <section aria-labelledby="titulo-novedades" className="flex flex-col gap-3">
          <h2 id="titulo-novedades" className="text-lg font-bold text-ink">
            {texto("estado.novedades")}
          </h2>
          <ul className="flex flex-col gap-2">
            {solicitud.novedades.map((novedad) => (
              <li key={novedad.fecha} className="rounded-[6px] border-l-4 border-state-pendiente-fg bg-state-pendiente-bg p-3">
                <p className="text-base text-ink">{novedad.descripcion}</p>
                <p className="mt-1 text-sm text-state-pendiente-fg">{fechaHora(novedad.fecha)}</p>
              </li>
            ))}
          </ul>
        </section>
      )}
      <ResumenCita cita={prefactura.cita} pago={prefactura.pago} texto={texto} />
      <div className="flex flex-col gap-3 sm:flex-row">
        {onCorregir !== null && (
          <Button size="lg" onPress={onCorregir}>
            {texto("estado.corregir")}
          </Button>
        )}
        <Button variant="outline" size="lg" onPress={onSalir}>
          {texto("estado.salir")}
        </Button>
      </div>
    </section>
  );
}
