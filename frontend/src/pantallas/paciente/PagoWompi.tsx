import { Alert, Button } from "@heroui/react";
import { useState } from "react";
import type { PasarelaNavegador } from "../../api/pasarelaWompi";
import type { IntencionPago } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, pesos } from "../../formato";
import { DatosPruebaWompi } from "./DatosPruebaWompi";

interface Props {
  readonly valor: number;
  readonly texto: Texto;
  readonly preparar: () => Promise<IntencionPago>;
  readonly pagar: (medio: string) => Promise<string | null>;
  readonly pasarela: PasarelaNavegador;
}

type Etapa = "inactivo" | "abriendo" | "verificando";

/**
 * Cada intento pide al backend una referencia y una firma nuevas, abre el widget de Wompi y, cuando Wompi finaliza la
 * transacción, el backend la verifica. Si el paciente cierra el widget sin pagar, el botón vuelve a quedar disponible.
 */
export function PagoWompi({ valor, texto, preparar, pagar, pasarela }: Props) {
  const [etapa, setEtapa] = useState<Etapa>("inactivo");
  const [rechazo, setRechazo] = useState<string | null>(null);

  const verificar = async (transaccion: string) => {
    setEtapa("verificando");
    setRechazo(await pagar(transaccion));
    setEtapa("inactivo");
  };

  const abrir = async () => {
    setEtapa("abriendo");
    setRechazo(null);
    try {
      const intencion = await preparar();
      if (intencion.pasarela !== "CHECKOUT") throw new Error("El backend dejó de ofrecer el checkout de Wompi");
      await pasarela.abrir(intencion.checkout, (transaccion) => void verificar(transaccion));
      setEtapa((actual) => (actual === "abriendo" ? "inactivo" : actual));
    } catch {
      setRechazo("copago.wompi.error");
      setEtapa("inactivo");
    }
  };

  return (
    <>
      <div className="flex flex-col items-start gap-2">
        <h2 className="text-base font-medium text-ink">{texto("copago.wompi.titulo")}</h2>
        <p className="text-sm text-ink-subtle">{texto("copago.wompi.sandbox")}</p>
        <DatosPruebaWompi texto={texto} />
      </div>
      {etapa === "verificando" && (
        <Alert status="accent" role="status" className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto("copago.wompi.verificando")}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
      {rechazo !== null && (
        <Alert status="danger" role="alert" className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto(rechazo)}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
      <Button size="lg" isPending={etapa !== "inactivo"} onPress={() => void abrir()} className="w-full sm:w-auto sm:self-start">
        {conValores(texto("copago.wompi.pagar"), { valor: pesos(valor) })}
      </Button>
    </>
  );
}
