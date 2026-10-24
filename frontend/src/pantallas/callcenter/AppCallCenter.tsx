import { Alert, Button } from "@heroui/react";
import { useState } from "react";
import type { SesionFacturador } from "../../api/clienteFacturador";
import type { ClientePersonal } from "../../api/clientePersonal";
import { esSesionVencida } from "../../api/http";
import type { CitaProgramada, ErrorNuevaCita, NuevaCita } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, fechaLarga } from "../../formato";
import { FormularioCita } from "./FormularioCita";

interface Props {
  readonly cliente: ClientePersonal;
  readonly sesion: SesionFacturador;
  readonly texto: Texto;
  readonly onSalir: (motivo: string | null) => void;
}

/** El agente asigna la cita, el sistema genera el ID de acceso y lo envía por SMS (flujo del paciente, pasos 2 y 3). */
export function AppCallCenter({ cliente, sesion, texto, onSalir }: Props) {
  const [programada, setProgramada] = useState<CitaProgramada | null>(null);
  const [errores, setErrores] = useState<readonly ErrorNuevaCita[]>([]);
  const [aviso, setAviso] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const programar = async (cita: NuevaCita) => {
    setEnviando(true);
    setAviso(null);
    try {
      const resultado = await cliente.programarCita(sesion.token, cita);
      if (resultado.tipo === "programada") setProgramada(resultado.programada);
      else setErrores(resultado.errores);
    } catch (error) {
      if (esSesionVencida(error)) return onSalir("facturador.acceso.sesion-vencida");
      setAviso("general.revise-conexion");
    } finally {
      setEnviando(false);
    }
  };

  return (
    <section aria-labelledby="titulo-callcenter" className="mx-auto flex w-full max-w-2xl flex-col gap-5">
      <h1 id="titulo-callcenter" className="text-xl font-bold text-brand-navy">
        {texto("callcenter.titulo")}
      </h1>
      <p className="text-sm text-ink-muted">{texto("callcenter.instruccion")}</p>
      {aviso !== null && (
        <Alert status="danger" role="alert">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto(aviso)}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
      {programada === null ? (
        <FormularioCita texto={texto} errores={errores} enviando={enviando} onProgramar={(cita) => void programar(cita)} />
      ) : (
        <ResultadoCita
          programada={programada}
          texto={texto}
          onOtra={() => {
            setProgramada(null);
            setErrores([]);
          }}
        />
      )}
    </section>
  );
}

function ResultadoCita({ programada, texto, onOtra }: { programada: CitaProgramada; texto: Texto; onOtra: () => void }) {
  const sms = programada.sms;
  return (
    <div role="status" className="aparicion-estado flex flex-col gap-4 rounded-[6px] border border-border p-4">
      <p className="text-base text-ink">
        {conValores(texto("callcenter.programada"), {
          identificacion: programada.identificacion,
          fecha: fechaLarga(programada.cita.fecha),
          medico: programada.cita.medico,
        })}
      </p>
      <div>
        <p className="text-sm font-medium text-ink-muted">{texto("callcenter.codigo")}</p>
        <p className="select-all text-3xl font-bold tracking-widest tabular-nums text-brand-navy">{programada.codigoAcceso}</p>
      </div>
      <p className="text-sm text-ink">{sms === null ? texto("callcenter.sms.sin-celular") : texto(`callcenter.sms.${sms.estado}`)}</p>
      <Button className="self-start" onPress={onOtra}>
        {texto("callcenter.otra")}
      </Button>
    </div>
  );
}
