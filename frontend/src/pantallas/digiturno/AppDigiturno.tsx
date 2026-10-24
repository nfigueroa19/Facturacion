import { Alert, Button, Form, Input, Label, TextField } from "@heroui/react";
import { useRef, useState, type FormEvent } from "react";
import type { SesionFacturador } from "../../api/clienteFacturador";
import type { ClientePersonal } from "../../api/clientePersonal";
import { esSesionVencida } from "../../api/http";
import type { Turno } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { TurnoAsignado } from "./TurnoAsignado";

interface Props {
  readonly cliente: ClientePersonal;
  readonly sesion: SesionFacturador;
  readonly texto: Texto;
  readonly onSalir: (motivo: string | null) => void;
}

/**
 * Simulación del digiturno de la sala de espera: lee la identificación, consulta la prefactura y asigna turno de
 * preconsulta o de facturación (RN-11). Con la validación aprobada registra la llegada (RN-14).
 */
export function AppDigiturno({ cliente, sesion, texto, onSalir }: Props) {
  const [turno, setTurno] = useState<Turno | null>(null);
  const [consultando, setConsultando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const campo = useRef<HTMLInputElement>(null);

  const leer = async (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    const identificacion = String(new FormData(evento.currentTarget).get("identificacion") ?? "").trim();
    if (identificacion === "") return setAviso("digiturno.identificacion-vacia");
    setConsultando(true);
    setAviso(null);
    try {
      setTurno(await cliente.atenderLlegada(sesion.token, identificacion));
    } catch (error) {
      if (esSesionVencida(error)) return onSalir("facturador.acceso.sesion-vencida");
      setAviso("general.revise-conexion");
    } finally {
      setConsultando(false);
    }
  };

  const siguiente = () => {
    setTurno(null);
    requestAnimationFrame(() => campo.current?.focus());
  };

  return (
    <section aria-labelledby="titulo-digiturno" className="mx-auto flex w-full max-w-xl flex-col gap-5">
      <h1 id="titulo-digiturno" className="text-2xl font-bold text-brand-navy">
        {texto("digiturno.titulo")}
      </h1>
      {turno !== null ? (
        <TurnoAsignado turno={turno} texto={texto} onSiguiente={siguiente} />
      ) : (
        <Form onSubmit={(evento) => void leer(evento)} className="flex flex-col gap-4">
          <TextField name="identificacion" inputMode="numeric" autoComplete="off" className="flex flex-col gap-2">
            <Label className="text-base font-medium text-ink">{texto("acceso.campo-identificacion")}</Label>
            <Input ref={campo} autoFocus className="min-h-14 text-2xl tabular-nums" />
          </TextField>
          {aviso !== null && (
            <Alert status="danger" role="alert">
              <Alert.Indicator />
              <Alert.Content>
                <Alert.Description>{texto(aviso)}</Alert.Description>
              </Alert.Content>
            </Alert>
          )}
          <Button type="submit" size="lg" isPending={consultando} className="self-start">
            {texto("digiturno.pedir")}
          </Button>
        </Form>
      )}
    </section>
  );
}
