import { Alert, Button } from "@heroui/react";
import { useEffect, useRef, useState } from "react";
import type { ClientePaciente } from "../../api/clientePaciente";
import { ErrorApi, esSesionVencida } from "../../api/http";
import { TIPOS_SOPORTE, type ErrorCampo, type Pago, type PrefacturaPaciente, type TipoSoporte } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { IndicadorPasos, type Paso } from "./IndicadorPasos";
import { PasoCopago } from "./PasoCopago";
import { PasoDatos } from "./PasoDatos";
import { PasoSoportes } from "./PasoSoportes";
import { ResumenCita } from "./ResumenCita";

interface Props {
  readonly cliente: ClientePaciente;
  readonly token: string;
  readonly prefactura: PrefacturaPaciente;
  readonly texto: Texto;
  /** Se llama tras radicar o si el servidor ya no admite cambios: en ambos casos se vuelve a leer la solicitud. */
  readonly onRadicada: () => void;
  readonly onSesionVencida: () => void;
}

/** Datos, soportes y copago (si la cita lo tiene). La solicitud se envía al final, en una sola petición. */
export function FlujoRadicacion({ cliente, token, prefactura, texto, onRadicada, onSesionVencida }: Props) {
  const pasos: readonly Paso[] = ["datos", "soportes", ...(prefactura.cita.copagoPesos === null ? [] : ["copago" as const])];
  const [indice, setIndice] = useState(0);
  const [valores, setValores] = useState<Readonly<Record<string, string>>>(prefactura.solicitud?.datos ?? {});
  const [erroresServidor, setErroresServidor] = useState<Readonly<Record<string, ErrorCampo>>>({});
  const [cargados, setCargados] = useState<ReadonlySet<TipoSoporte>>(new Set(prefactura.soportes));
  const [pago, setPago] = useState<Pago | null>(prefactura.pago);
  const [aviso, setAviso] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const titulo = useRef<HTMLHeadingElement>(null);
  const paso = pasos[indice] ?? "datos";
  const esUltimo = indice === pasos.length - 1;

  // Al cambiar de paso, el foco va al título para que el lector de pantalla anuncie dónde está el paciente.
  const primerRender = useRef(true);
  useEffect(() => {
    if (primerRender.current) primerRender.current = false;
    else titulo.current?.focus();
  }, [indice]);

  const irA = (destino: Paso, clave: string | null) => {
    setIndice(Math.max(0, pasos.indexOf(destino)));
    setAviso(clave);
  };

  const conSesion = async <T,>(accion: () => Promise<T>): Promise<T> => {
    try {
      return await accion();
    } catch (error) {
      if (esSesionVencida(error)) onSesionVencida();
      throw error;
    }
  };

  const enviar = async () => {
    setEnviando(true);
    setAviso(null);
    try {
      const resultado = await conSesion(() => cliente.radicar(token, valores));
      if (resultado.tipo === "radicada") return onRadicada();
      setErroresServidor(resultado.errores);
      if (Object.keys(resultado.errores).length > 0) return irA("datos", "envio.datos-invalidos");
      setCargados(new Set(TIPOS_SOPORTE.filter((tipo) => !resultado.soportesFaltantes.includes(tipo))));
      irA("soportes", "soportes.faltan");
    } catch (error) {
      if (error instanceof ErrorApi && error.estado === 409) onRadicada();
      else setAviso("envio.error-conexion");
    } finally {
      setEnviando(false);
    }
  };

  const continuar = () => {
    if (paso === "soportes" && TIPOS_SOPORTE.some((tipo) => !cargados.has(tipo))) return setAviso("soportes.faltan");
    if (esUltimo) return void enviar();
    setAviso(null);
    setIndice(indice + 1);
  };

  const pagar = async () => {
    try {
      setPago(await conSesion(() => cliente.pagarCopago(token)));
      return true;
    } catch {
      return false;
    }
  };

  const sinPagar = paso === "copago" && pago === null;
  return (
    <section aria-labelledby="titulo-paso" className="aparicion-estado flex flex-col gap-6">
      <IndicadorPasos pasos={[...pasos, "confirmacion"]} actual={indice} texto={texto} />
      <h1 id="titulo-paso" ref={titulo} tabIndex={-1} className="text-2xl font-bold text-brand-navy outline-none">
        {texto(`${paso}.titulo`)}
      </h1>
      {aviso !== null && (
        <Alert status="danger" role="alert" className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto(aviso)}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
      {paso === "datos" && (
        <>
          <ResumenCita cita={prefactura.cita} pago={pago} texto={texto} />
          <PasoDatos
            campos={prefactura.formulario}
            valores={valores}
            erroresServidor={erroresServidor}
            texto={texto}
            onCambiar={(clave, valor) => {
              setValores((actuales) => ({ ...actuales, [clave]: valor }));
              setErroresServidor(({ [clave]: _, ...resto }) => resto);
            }}
            onContinuar={continuar}
          />
        </>
      )}
      {paso === "soportes" && (
        <PasoSoportes
          tamanoMaximo={prefactura.tamanoMaximoSoporteBytes}
          cargados={cargados}
          texto={texto}
          subir={(tipo, archivo) => conSesion(() => cliente.subirSoporte(token, tipo, archivo))}
          onCargado={(tipo) => {
            setCargados((actuales) => new Set([...actuales, tipo]));
            setAviso(null);
          }}
        />
      )}
      {paso === "copago" && prefactura.cita.copagoPesos !== null && (
        <PasoCopago valor={prefactura.cita.copagoPesos} pago={pago} texto={texto} pagar={pagar} />
      )}
      {paso !== "datos" && (
        <div className="flex flex-col-reverse gap-3 sm:flex-row">
          <Button variant="outline" size="lg" onPress={() => irA(pasos[indice - 1] ?? "datos", null)} isDisabled={enviando} className="w-full sm:w-auto">
            {texto("pasos.volver")}
          </Button>
          <Button variant={sinPagar ? "outline" : "primary"} size="lg" isPending={enviando} onPress={continuar} className="w-full sm:w-auto">
            {texto(sinPagar ? "copago.sin-pagar" : esUltimo ? "envio.boton" : "pasos.continuar")}
          </Button>
        </div>
      )}
    </section>
  );
}
