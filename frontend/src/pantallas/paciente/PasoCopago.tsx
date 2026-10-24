import { Alert, Button, Label, Radio, RadioGroup } from "@heroui/react";
import { useState } from "react";
import type { Pago } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, pesos } from "../../formato";

/**
 * Tarjetas de la pasarela de pruebas. En producción la pasarela tokeniza la tarjeta en el navegador y aquí solo
 * viaja el token; con la de pruebas cada token da un resultado fijo, como las tarjetas de prueba de Wompi.
 */
const TARJETAS_PRUEBA = ["tok_prueba_aprobada", "tok_prueba_rechazada", "tok_prueba_fondos_insuficientes", "tok_prueba_error"] as const;

interface Props {
  readonly valor: number;
  readonly pago: Pago | null;
  readonly texto: Texto;
  /** Devuelve null si el pago quedó aprobado, o la clave del texto que explica el rechazo. */
  readonly pagar: (medio: string) => Promise<string | null>;
}

/** El pago en la web es opcional (RN-10): si la pasarela lo rechaza, el paciente reintenta o paga en ventanilla. */
export function PasoCopago({ valor, pago, texto, pagar }: Props) {
  const [medio, setMedio] = useState<string>(TARJETAS_PRUEBA[0]);
  const [pagando, setPagando] = useState(false);
  const [rechazo, setRechazo] = useState<string | null>(null);

  const alPagar = async () => {
    setPagando(true);
    setRechazo(null);
    setRechazo(await pagar(medio));
    setPagando(false);
  };

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
          <RadioGroup value={medio} onChange={setMedio} className="flex flex-col gap-3">
            <Label className="text-base font-medium text-ink">{texto("copago.tarjeta.titulo")}</Label>
            <p className="text-sm text-ink-subtle">{texto("copago.simulado")}</p>
            {TARJETAS_PRUEBA.map((token) => (
              <Radio key={token} value={token} className="min-h-11">
                <Radio.Content className="text-base text-ink">
                  <Radio.Control>
                    <Radio.Indicator />
                  </Radio.Control>
                  <Label>{texto(`copago.tarjeta.${token}`)}</Label>
                </Radio.Content>
              </Radio>
            ))}
          </RadioGroup>
          {rechazo !== null && (
            <Alert status="danger" role="alert" className="aparicion-estado">
              <Alert.Indicator />
              <Alert.Content>
                <Alert.Description>{texto(rechazo)}</Alert.Description>
              </Alert.Content>
            </Alert>
          )}
          <Button size="lg" isPending={pagando} onPress={() => void alPagar()} className="w-full sm:w-auto sm:self-start">
            {conValores(texto("copago.pagar"), { valor: pesos(valor) })}
          </Button>
        </>
      )}
    </div>
  );
}
