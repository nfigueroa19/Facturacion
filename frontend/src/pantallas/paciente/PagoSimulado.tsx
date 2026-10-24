import { Alert, Button, Label, Radio, RadioGroup } from "@heroui/react";
import { useState } from "react";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, pesos } from "../../formato";

/** Cada token de la pasarela simulada del backend da un resultado fijo, como las tarjetas de prueba de Wompi. */
const TARJETAS_PRUEBA = ["tok_prueba_aprobada", "tok_prueba_rechazada", "tok_prueba_fondos_insuficientes", "tok_prueba_error"] as const;

interface Props {
  readonly valor: number;
  readonly texto: Texto;
  readonly pagar: (medio: string) => Promise<string | null>;
}

/** Pasarela simulada: el backend no tiene las llaves de Wompi y el paciente elige una tarjeta de prueba. */
export function PagoSimulado({ valor, texto, pagar }: Props) {
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
    <>
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
  );
}
