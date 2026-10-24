import { Skeleton } from "@heroui/react";
import { useEffect, useState } from "react";
import type { ClienteFacturador, SesionFacturador } from "../../api/clienteFacturador";
import { esSesionVencida } from "../../api/http";
import { ErrorCarga } from "../../componentes/ErrorCarga";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores } from "../../formato";
import { useCarga } from "../../useCarga";
import { BarraFiltros } from "./BarraFiltros";
import { DetalleSolicitud } from "./DetalleSolicitud";
import { FILTROS_INICIALES, filtrar, type Filtros } from "./filtros";
import { TablaSolicitudes, type PropsTabla } from "./TablaSolicitudes";

interface Props {
  readonly cliente: ClienteFacturador;
  readonly sesion: SesionFacturador;
  readonly texto: Texto;
  readonly onSalir: (motivo: string | null) => void;
}

export function PanelSolicitudes({ cliente, sesion, texto, onSalir }: Props) {
  const [filtros, setFiltros] = useState<Filtros>(FILTROS_INICIALES);
  const [seleccionada, setSeleccionada] = useState<string | null>(null);
  const { carga, recargar } = useCarga((senal) => cliente.solicitudes(sesion.token, filtros.estado, senal), [filtros.estado]);
  const vencida = carga.tipo === "error" && esSesionVencida(carga.error);
  const sesionVencida = () => onSalir("facturador.acceso.sesion-vencida");

  useEffect(() => {
    if (vencida) onSalir("facturador.acceso.sesion-vencida");
  }, [vencida, onSalir]);

  return (
    <div className="flex flex-col gap-5">
      <h1 className="text-xl font-bold text-brand-navy">{texto("solicitudes.titulo")}</h1>
      <BarraFiltros filtros={filtros} texto={texto} onCambiar={setFiltros} />
      <div className={seleccionada === null ? "" : "grid items-start gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(0,560px)]"}>
        <section aria-label={texto("solicitudes.titulo")} className="flex min-w-0 flex-col gap-2">
          {carga.tipo === "cargando" || vencida ? (
            <div role="status" className="flex flex-col gap-2">
              <span className="sr-only">{texto("solicitudes.cargando")}</span>
              {[0, 1, 2, 3].map((fila) => (
                <Skeleton key={fila} className="h-10 w-full rounded-[6px]" />
              ))}
            </div>
          ) : carga.tipo === "error" ? (
            <ErrorCarga texto={texto} titulo="solicitudes.error" onReintentar={recargar} />
          ) : (
            <ListaFiltrada
              solicitudes={filtrar(carga.valor, filtros)}
              seleccionada={seleccionada}
              texto={texto}
              onSeleccionar={setSeleccionada}
            />
          )}
        </section>
        {seleccionada !== null && (
          <DetalleSolicitud
            key={seleccionada}
            cliente={cliente}
            token={sesion.token}
            consecutivo={seleccionada}
            texto={texto}
            onCerrar={() => setSeleccionada(null)}
            onCambio={recargar}
            onSesionVencida={sesionVencida}
          />
        )}
      </div>
    </div>
  );
}

function ListaFiltrada(props: PropsTabla) {
  return (
    <>
      <p role="status" className="text-sm text-ink-muted">
        {conValores(props.texto("solicitudes.total"), { n: props.solicitudes.length })}
      </p>
      {props.solicitudes.length === 0 ? (
        <p className="rounded-[6px] border border-border p-4 text-sm text-ink">{props.texto("solicitudes.vacio")}</p>
      ) : (
        <TablaSolicitudes {...props} />
      )}
    </>
  );
}
