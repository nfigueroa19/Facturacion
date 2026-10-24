import { Button, Drawer, Skeleton } from "@heroui/react";
import { MessageSquareText } from "lucide-react";
import { useEffect } from "react";
import type { ClientePersonal } from "../../api/clientePersonal";
import { esSesionVencida } from "../../api/http";
import type { Comunicacion, EstadoEnvio } from "../../api/tipos";
import { ErrorCarga } from "../../componentes/ErrorCarga";
import type { Texto } from "../../contenido/ContenidoContexto";
import { fechaHora } from "../../formato";
import { useCarga } from "../../useCarga";

interface Props {
  readonly cliente: ClientePersonal;
  readonly token: string;
  readonly texto: Texto;
  readonly onSesionVencida: () => void;
}

/** Panel de auditoría: los SMS enviados, simulados o fallidos. Con SMS simulados es la única forma de leerlos. */
export function PanelComunicaciones({ cliente, token, texto, onSesionVencida }: Props) {
  return (
    <Drawer>
      <Button variant="outline" size="sm">
        <MessageSquareText aria-hidden="true" className="size-4" />
        {texto("comunicaciones.abrir")}
      </Button>
      <Drawer.Backdrop>
        <Drawer.Content placement="right">
          <Drawer.Dialog className="flex h-full w-full max-w-md flex-col">
            <Drawer.CloseTrigger aria-label={texto("comunicaciones.cerrar")} />
            <Drawer.Header>
              <Drawer.Heading className="text-lg font-bold text-brand-navy">{texto("comunicaciones.titulo")}</Drawer.Heading>
              <p className="text-sm text-ink-muted">{texto("comunicaciones.ayuda")}</p>
            </Drawer.Header>
            <Drawer.Body>
              <ListaComunicaciones cliente={cliente} token={token} texto={texto} onSesionVencida={onSesionVencida} />
            </Drawer.Body>
          </Drawer.Dialog>
        </Drawer.Content>
      </Drawer.Backdrop>
    </Drawer>
  );
}

// Se monta al abrir el panel: cada apertura vuelve a pedir la lista.
function ListaComunicaciones({ cliente, token, texto, onSesionVencida }: Props) {
  const { carga, recargar } = useCarga((senal) => cliente.comunicaciones(token, senal), [token]);
  const vencida = carga.tipo === "error" && esSesionVencida(carga.error);
  useEffect(() => {
    if (vencida) onSesionVencida();
  }, [vencida, onSesionVencida]);

  if (carga.tipo === "cargando") {
    return (
      <div role="status" className="flex flex-col gap-2">
        <span className="sr-only">{texto("comunicaciones.cargando")}</span>
        <Skeleton className="h-20 w-full rounded-[6px]" />
        <Skeleton className="h-20 w-full rounded-[6px]" />
      </div>
    );
  }
  if (carga.tipo === "error") return <ErrorCarga texto={texto} titulo="comunicaciones.error" onReintentar={recargar} />;
  return (
    <div className="flex flex-col gap-3">
      <Button variant="outline" size="sm" className="self-start" onPress={recargar}>
        {texto("comunicaciones.actualizar")}
      </Button>
      {carga.valor.length === 0 ? (
        <p className="rounded-[6px] border border-border p-4 text-sm text-ink">{texto("comunicaciones.vacio")}</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {carga.valor.map((comunicacion) => (
            <ItemComunicacion key={`${comunicacion.fecha}-${comunicacion.cita}-${comunicacion.motivo}`} comunicacion={comunicacion} texto={texto} />
          ))}
        </ul>
      )}
    </div>
  );
}

const ESTILO_ENVIO: Readonly<Record<EstadoEnvio, string>> = {
  ENVIADO: "bg-state-aprobado-bg text-state-aprobado-fg",
  SIMULADO: "bg-state-radicado-bg text-state-radicado-fg",
  FALLIDO: "bg-danger-bg text-danger-fg",
};

function ItemComunicacion({ comunicacion, texto }: { comunicacion: Comunicacion; texto: Texto }) {
  return (
    <li className="flex flex-col gap-1.5 rounded-[6px] border border-border p-3 text-sm">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <span className="font-medium tabular-nums text-ink">{comunicacion.destino}</span>
        <span className={`rounded-full px-2.5 py-0.5 text-sm font-medium ${ESTILO_ENVIO[comunicacion.estado]}`}>
          {texto(`comunicaciones.estado.${comunicacion.estado}`)}
        </span>
      </div>
      <p className="text-ink">{comunicacion.texto}</p>
      <p className="text-ink-subtle">
        {texto(`comunicaciones.motivo.${comunicacion.motivo}`)}, {fechaHora(comunicacion.fecha)}
      </p>
    </li>
  );
}
