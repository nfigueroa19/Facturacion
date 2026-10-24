import { Alert, Skeleton } from "@heroui/react";
import type { PasarelaNavegador } from "../../api/pasarelaWompi";
import type { IntencionPago, Pago } from "../../api/tipos";
import { ErrorCarga } from "../../componentes/ErrorCarga";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, pesos } from "../../formato";
import { useCarga } from "../../useCarga";
import { PagoSimulado } from "./PagoSimulado";
import { PagoWompi } from "./PagoWompi";

interface Props {
  readonly valor: number;
  readonly pago: Pago | null;
  readonly texto: Texto;
  readonly preparar: () => Promise<IntencionPago>;
  /** Devuelve null si el pago quedó aprobado, o la clave del texto que explica el rechazo. */
  readonly pagar: (medio: string) => Promise<string | null>;
  readonly pasarela: PasarelaNavegador;
}

/** El pago en la web es opcional (RN-10): si la pasarela lo rechaza, el paciente reintenta o paga en ventanilla. */
export function PasoCopago({ valor, pago, texto, preparar, pagar, pasarela }: Props) {
  return (
    <div className="flex flex-col gap-4">
      <div className="rounded-[6px] border border-border p-4">
        <p className="text-sm font-medium text-ink-muted">{texto("copago.valor")}</p>
        <p className="text-2xl font-bold tabular-nums text-ink">{pesos(valor)}</p>
      </div>
      {pago !== null ? (
        <Alert status="success" role="status" className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{conValores(texto("copago.pagado"), { referencia: pago.referencia })}</Alert.Description>
          </Alert.Content>
        </Alert>
      ) : (
        <>
          <p className="text-base text-ink-muted">{texto("copago.ventanilla")}</p>
          <MedioDePago valor={valor} texto={texto} preparar={preparar} pagar={pagar} pasarela={pasarela} />
        </>
      )}
    </div>
  );
}

/** El backend decide la pasarela: Wompi sandbox si tiene sus llaves, la simulada si no. */
function MedioDePago({ valor, texto, preparar, pagar, pasarela }: Omit<Props, "pago">) {
  const { carga, recargar } = useCarga(() => preparar(), []);
  switch (carga.tipo) {
    case "cargando":
      return (
        <div aria-busy="true" aria-label={texto("copago.medio.cargando")} className="flex flex-col gap-3">
          <Skeleton className="h-5 w-48 rounded-[6px]" />
          <Skeleton className="h-11 w-full rounded-[6px] sm:w-56" />
        </div>
      );
    case "error":
      return <ErrorCarga texto={texto} titulo="copago.medio.error" onReintentar={recargar} />;
    case "lista":
      return carga.valor.pasarela === "CHECKOUT" ? (
        <PagoWompi valor={valor} texto={texto} preparar={preparar} pagar={pagar} pasarela={pasarela} />
      ) : (
        <PagoSimulado valor={valor} texto={texto} pagar={pagar} />
      );
  }
}
