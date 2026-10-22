import { Alert, Button, Skeleton } from "@heroui/react";
import { useEffect, useRef, useState, type ReactNode } from "react";
import type { ClienteFacturador } from "../../api/clienteFacturador";
import { ErrorApi, esSesionVencida } from "../../api/http";
import type { DetalleSolicitud as Detalle } from "../../api/tipos";
import { ErrorCarga } from "../../componentes/ErrorCarga";
import { EstadoSolicitud } from "../../componentes/EstadoSolicitud";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, fechaHora, fechaLarga } from "../../formato";
import { useCarga } from "../../useCarga";
import { textoCopago } from "../../componentes/textoCopago";
import { AccionesSolicitud } from "./AccionesSolicitud";
import { VisorSoportes } from "./VisorSoportes";

interface Props {
  readonly cliente: ClienteFacturador;
  readonly token: string;
  readonly consecutivo: string;
  readonly texto: Texto;
  readonly onCerrar: () => void;
  /** La solicitud cambió de estado: la lista se vuelve a pedir. */
  readonly onCambio: () => void;
  readonly onSesionVencida: () => void;
}

type Aviso = { readonly exito: boolean; readonly clave: string };

export function DetalleSolicitud({ cliente, token, consecutivo, texto, onCerrar, onCambio, onSesionVencida }: Props) {
  const { carga, recargar } = useCarga((senal) => cliente.detalle(token, consecutivo, senal), [consecutivo]);
  const [aviso, setAviso] = useState<Aviso | null>(null);
  const titulo = useRef<HTMLHeadingElement>(null);
  const vencida = carga.tipo === "error" && esSesionVencida(carga.error);

  useEffect(() => titulo.current?.focus(), []);
  useEffect(() => {
    if (vencida) onSesionVencida();
  }, [vencida, onSesionVencida]);

  /**
   * Ejecuta una acción del facturador y vuelve a pedir el detalle y la lista, haya funcionado o no.
   * El botón usado desaparece con el cambio de estado, así que el foco vuelve al título del detalle.
   */
  const ejecutar = async (accion: () => Promise<void>, exito: string) => {
    setAviso(null);
    try {
      await accion();
      setAviso({ exito: true, clave: exito });
    } catch (error) {
      if (esSesionVencida(error)) return onSesionVencida();
      setAviso({ exito: false, clave: error instanceof ErrorApi && error.estado === 409 ? "acciones.conflicto" : "acciones.error" });
    }
    titulo.current?.focus();
    recargar();
    onCambio();
  };

  return (
    <aside aria-labelledby="titulo-detalle" className="flex min-w-0 flex-col gap-5 rounded-[6px] border border-border p-4 xl:sticky xl:top-4">
      <div className="flex items-start justify-between gap-3">
        <div className="flex flex-col gap-2">
          <h2 id="titulo-detalle" ref={titulo} tabIndex={-1} className="text-lg font-bold text-brand-navy outline-none">
            {conValores(texto("detalle.titulo"), { consecutivo })}
          </h2>
          {carga.tipo === "lista" && <EstadoSolicitud estado={carga.valor.resumen.estado} texto={texto} />}
        </div>
        <Button variant="outline" size="sm" onPress={onCerrar}>
          {texto("detalle.cerrar")}
        </Button>
      </div>
      {aviso !== null && (
        <Alert status={aviso.exito ? "success" : "danger"} role={aviso.exito ? "status" : "alert"} className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto(aviso.clave)}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
      {carga.tipo === "lista" ? (
        <ContenidoDetalle
          detalle={carga.valor}
          texto={texto}
          visor={
            <VisorSoportes
              consecutivo={consecutivo}
              cargados={carga.valor.resumen.soportes}
              texto={texto}
              pedir={(tipo, senal) => cliente.soporte(token, consecutivo, tipo, senal)}
            />
          }
          acciones={
            <AccionesSolicitud
              key={carga.valor.resumen.estado}
              detalle={carga.valor}
              texto={texto}
              aprobar={(revisados) => ejecutar(() => cliente.aprobar(token, consecutivo, revisados), "acciones.hecho.aprobar")}
              registrarNovedad={(descripcion) =>
                ejecutar(() => cliente.registrarNovedad(token, consecutivo, descripcion), "acciones.hecho.novedad")
              }
              confirmarLlegada={() => ejecutar(() => cliente.confirmarLlegada(token, consecutivo), "acciones.hecho.llegada")}
            />
          }
        />
      ) : carga.tipo === "error" && !vencida ? (
        <ErrorCarga texto={texto} titulo="detalle.error" onReintentar={recargar} />
      ) : (
        <div role="status" className="flex flex-col gap-3">
          <span className="sr-only">{texto("detalle.cargando")}</span>
          <Skeleton className="h-24 w-full rounded-[6px]" />
          <Skeleton className="h-48 w-full rounded-[6px]" />
        </div>
      )}
    </aside>
  );
}

function Seccion({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section className="flex flex-col gap-2 border-t border-border pt-4">
      <h3 className="text-sm font-bold uppercase tracking-wide text-ink-muted">{titulo}</h3>
      {children}
    </section>
  );
}

function Datos({ filas }: { filas: readonly (readonly [string, string])[] }) {
  return (
    <dl className="grid grid-cols-[minmax(0,2fr)_minmax(0,3fr)] gap-x-4 gap-y-1.5 text-sm">
      {filas.map(([etiqueta, valor]) => (
        <div key={etiqueta} className="contents">
          <dt className="text-ink-muted">{etiqueta}</dt>
          <dd className="break-words font-medium text-ink">{valor}</dd>
        </div>
      ))}
    </dl>
  );
}

interface PropsContenido {
  readonly detalle: Detalle;
  readonly texto: Texto;
  readonly visor: ReactNode;
  readonly acciones: ReactNode;
}

function ContenidoDetalle({ detalle, texto, visor, acciones }: PropsContenido) {
  const { resumen } = detalle;
  return (
    <>
      <Seccion titulo={texto("detalle.cita")}>
        <Datos
          filas={[
            [texto("columna.identificacion"), resumen.identificacion],
            [texto("cita.fecha"), fechaLarga(resumen.cita.fecha)],
            [texto("cita.medico"), resumen.cita.medico],
            [texto("cita.especialidad"), resumen.cita.especialidad],
            [texto("cita.regimen"), texto(`regimen.${resumen.cita.regimen}`)],
            [texto("cita.copago"), textoCopago(resumen.cita, resumen.pago, texto)],
            [texto("columna.enviada"), fechaHora(resumen.fechaEnvio)],
          ]}
        />
      </Seccion>
      <Seccion titulo={texto("detalle.datos")}>
        <Datos filas={detalle.datos.map((dato) => [dato.etiqueta, dato.valor] as const)} />
      </Seccion>
      <Seccion titulo={texto("detalle.soportes")}>{visor}</Seccion>
      <Seccion titulo={texto("detalle.novedades")}>
        {detalle.novedades.length === 0 ? (
          <p className="text-sm text-ink-muted">{texto("detalle.sin-novedades")}</p>
        ) : (
          <ul className="flex flex-col gap-2 text-sm">
            {detalle.novedades.map((novedad) => (
              <li key={novedad.fecha} className="rounded-[6px] bg-state-pendiente-bg p-2 text-ink">
                {novedad.descripcion}
                <span className="mt-0.5 block text-state-pendiente-fg">
                  {novedad.facturador}, {fechaHora(novedad.fecha)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </Seccion>
      {detalle.validacion !== null && (
        <p className="text-sm text-ink-muted">
          {conValores(texto("detalle.validacion"), { facturador: detalle.validacion.facturador, fecha: fechaHora(detalle.validacion.fecha) })}
        </p>
      )}
      <Seccion titulo={texto("detalle.acciones")}>{acciones}</Seccion>
    </>
  );
}
