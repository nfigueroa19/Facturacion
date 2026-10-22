import { Alert, Button } from "@heroui/react";
import { useState } from "react";
import type { Pago } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, pesos } from "../../formato";

interface Props {
  readonly valor: number;
  readonly pago: Pago | null;
  readonly texto: Texto;
  /** Devuelve false si el pago no se pudo hacer. */
  readonly pagar: () => Promise<boolean>;
}

/** El pago en la web es opcional (RN-10): sin pago, el copago se cobra en ventanilla. */
export function PasoCopago({ valor, pago, texto, pagar }: Props) {
  const [pagando, setPagando] = useState(false);
  const [fallo, setFallo] = useState(false);

  const alPagar = async () => {
    setPagando(true);
    setFallo(false);
    setFallo(!(await pagar()));
    setPagando(false);
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="rounded-[6px] border border-border p-4">
        <p className="text-sm font-medium text-ink-muted">{texto("copago.valor")}</p>
        <p className="text-2xl font-bold tabular-nums text-ink">{pesos(valor)}</p>
      </div>
      <Alert role="note">
        <Alert.Indicator />
        <Alert.Content>
          <Alert.Description>{texto("copago.simulado")}</Alert.Description>
        </Alert.Content>
      </Alert>
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
          {fallo && (
            <Alert status="danger" role="alert" className="aparicion-estado">
              <Alert.Indicator />
              <Alert.Content>
                <Alert.Description>{texto("copago.error")}</Alert.Description>
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
