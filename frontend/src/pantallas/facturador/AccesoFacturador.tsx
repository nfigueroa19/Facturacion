import { Alert, Button, Form } from "@heroui/react";
import { useState, type FormEvent } from "react";
import type { SesionFacturador } from "../../api/clienteFacturador";
import { CampoObligatorio } from "../../componentes/CampoObligatorio";
import type { Texto } from "../../contenido/ContenidoContexto";

interface Props {
  readonly texto: Texto;
  readonly aviso: string | null;
  readonly iniciarSesion: (usuario: string, clave: string) => Promise<SesionFacturador | null>;
  readonly onIngreso: (sesion: SesionFacturador) => void;
}

export function AccesoFacturador({ texto, aviso, iniciarSesion, onIngreso }: Props) {
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const enviar = async (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    const datos = new FormData(evento.currentTarget);
    setEnviando(true);
    setError(null);
    try {
      const sesion = await iniciarSesion(String(datos.get("usuario")), String(datos.get("clave")));
      if (sesion !== null) return onIngreso(sesion);
      setError("facturador.acceso.error");
    } catch {
      setError("general.revise-conexion");
    }
    setEnviando(false);
  };

  const mensaje = error ?? aviso;
  return (
    <section aria-labelledby="titulo-acceso-facturador" className="aparicion-estado mx-auto flex w-full max-w-sm flex-col gap-5">
      <h1 id="titulo-acceso-facturador" className="text-xl font-bold text-brand-navy">
        {texto("facturador.acceso.titulo")}
      </h1>
      {mensaje !== null && (
        <Alert status={error === null ? "default" : "danger"} role={error === null ? "status" : "alert"}>
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto(mensaje)}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
      <Form onSubmit={enviar} className="flex flex-col gap-4">
        <CampoObligatorio nombre="usuario" etiqueta={texto("facturador.acceso.usuario")} mensajeVacio={texto("acceso.dato-obligatorio")} autoComplete="username" />
        <CampoObligatorio
          nombre="clave"
          tipo="password"
          etiqueta={texto("facturador.acceso.clave")}
          mensajeVacio={texto("acceso.dato-obligatorio")}
          autoComplete="current-password"
        />
        <Button type="submit" isPending={enviando} className="self-start">
          {texto("facturador.acceso.boton")}
        </Button>
      </Form>
    </section>
  );
}
