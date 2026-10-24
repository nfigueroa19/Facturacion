import { Alert, Button, Form, Label, Radio, RadioGroup } from "@heroui/react";
import { useState, type FormEvent } from "react";
import { ErrorWompi, type MedioWompi, type PasarelaNavegador } from "../../api/pasarelaWompi";
import type { CheckoutPago } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, pesos } from "../../formato";
import { AceptacionWompi, type Aceptacion } from "./AceptacionWompi";
import { CampoWompi, CamposNequi, CamposPse, CamposTarjeta, sinEspacios } from "./CamposWompi";
import { DatosPruebaWompi } from "./DatosPruebaWompi";

interface Props {
  readonly valor: number;
  /** Primer checkout: llave pública y enlaces de los contratos. Cada intento pide uno nuevo, con tokens sin usar. */
  readonly checkout: CheckoutPago;
  readonly texto: Texto;
  readonly preparar: () => Promise<CheckoutPago>;
  readonly pagar: (transaccion: string) => Promise<string | null>;
  readonly pasarela: PasarelaNavegador;
}

const MEDIOS = ["CARD", "NEQUI", "PSE"] as const;
type TipoMedio = (typeof MEDIOS)[number];
type Etapa = "inactivo" | "procesando" | "verificando";

function leerMedio(tipo: TipoMedio, datos: FormData): MedioWompi {
  const campo = (nombre: string) => String(datos.get(nombre) ?? "").trim();
  switch (tipo) {
    case "CARD":
      return { tipo, numero: sinEspacios(campo("numero")), titular: campo("titular"), mes: campo("mes").padStart(2, "0"), anio: campo("anio"), cvc: campo("cvc") };
    case "NEQUI":
      return { tipo, celular: campo("celular") };
    case "PSE":
      return { tipo, banco: campo("banco"), tipoDocumento: campo("tipoDocumento"), documento: campo("documento") };
  }
}

/**
 * El navegador tokeniza la tarjeta y crea la transacción en Wompi sandbox con la referencia y el monto del backend; el
 * backend verifica la transacción antes de dar el copago por pagado. Un rechazo deja reintentar con otro medio.
 */
export function PagoWompi({ valor, checkout, texto, preparar, pagar, pasarela }: Props) {
  const [medio, setMedio] = useState<TipoMedio>("CARD");
  const [aceptacion, setAceptacion] = useState<Aceptacion>({ terminos: false, datos: false });
  const [faltaAceptar, setFaltaAceptar] = useState(false);
  const [etapa, setEtapa] = useState<Etapa>("inactivo");
  const [rechazo, setRechazo] = useState<string | null>(null);

  const cobrar = async (datos: FormData) => {
    setEtapa("procesando");
    setRechazo(null);
    try {
      const intento = await preparar();
      const transaccion = await pasarela.pagar(intento, leerMedio(medio, datos), String(datos.get("correo") ?? "").trim());
      setEtapa("verificando");
      setRechazo(await pagar(transaccion));
    } catch (error) {
      setRechazo(error instanceof ErrorWompi ? "copago.wompi.datos-invalidos" : "copago.wompi.error");
    }
    setEtapa("inactivo");
  };

  const enviar = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    const aceptada = aceptacion.terminos && aceptacion.datos;
    setFaltaAceptar(!aceptada);
    if (aceptada) void cobrar(new FormData(evento.currentTarget));
  };

  return (
    <Form onSubmit={enviar} className="flex flex-col gap-6">
      <div className="flex flex-col items-start gap-2">
        <h2 className="text-lg font-bold text-ink">{texto("copago.wompi.titulo")}</h2>
        <p className="text-sm text-ink-subtle">{texto("copago.wompi.sandbox")}</p>
        <DatosPruebaWompi texto={texto} />
      </div>
      <RadioGroup value={medio} onChange={(valor) => setMedio(valor as TipoMedio)} orientation="horizontal" className="flex flex-col gap-2">
        <Label className="text-base font-medium text-ink">{texto("copago.wompi.medio")}</Label>
        <div className="flex flex-wrap gap-x-6 gap-y-2">
          {MEDIOS.map((opcion) => (
            <Radio key={opcion} value={opcion} className="min-h-11">
              <Radio.Content className="min-h-11 items-center text-base text-ink">
                <Radio.Control>
                  <Radio.Indicator />
                </Radio.Control>
                <Label>{texto(`copago.wompi.medio.${opcion}`)}</Label>
              </Radio.Content>
            </Radio>
          ))}
        </div>
      </RadioGroup>
      {medio === "CARD" && <CamposTarjeta texto={texto} />}
      {medio === "NEQUI" && <CamposNequi texto={texto} />}
      {medio === "PSE" && <CamposPse texto={texto} bancos={() => pasarela.bancosPse(checkout.llavePublica)} />}
      <CampoWompi nombre="correo" texto={texto} tipo="email" modo="email" autoComplete="email" largo={120} valido={(valor) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor)} />
      <AceptacionWompi contratos={checkout.aceptacion} aceptacion={aceptacion} faltaAceptar={faltaAceptar} texto={texto} onCambiar={setAceptacion} />
      {etapa !== "inactivo" && (
        <Alert status="accent" role="status" className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto(`copago.wompi.${etapa}`)}</Alert.Description>
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
      <Button type="submit" size="lg" isPending={etapa !== "inactivo"} className="w-full sm:w-auto sm:self-start">
        {conValores(texto("copago.wompi.pagar"), { valor: pesos(valor) })}
      </Button>
    </Form>
  );
}
