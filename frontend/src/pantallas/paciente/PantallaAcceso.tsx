import { Alert, Button, Form } from "@heroui/react";
import { useState, type FormEvent } from "react";
import { CampoObligatorio } from "../../componentes/CampoObligatorio";
import type { Texto } from "../../contenido/ContenidoContexto";

export type ResultadoIngreso = "ingreso" | "credenciales" | "conexion";

interface Props {
  readonly texto: Texto;
  readonly onIngresar: (identificacion: string, codigoAcceso: string) => Promise<ResultadoIngreso>;
  /** Clave de contenido.json del aviso que trae la pantalla, por ejemplo una sesión vencida. */
  readonly aviso: string | null;
}

export function PantallaAcceso({ texto, onIngresar, aviso }: Props) {
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const enviar = async (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    const datos = new FormData(evento.currentTarget);
    setEnviando(true);
    setError(null);
    const resultado = await onIngresar(String(datos.get("identificacion")), String(datos.get("idAcceso")));
    if (resultado !== "ingreso") {
      setError(resultado === "credenciales" ? "acceso.error-credenciales" : "acceso.error-conexion");
      setEnviando(false);
    }
  };

  const mensaje = error ?? aviso;
  return (
    <section aria-labelledby="titulo-acceso" className="aparicion-estado flex flex-col gap-6">
      <h1 id="titulo-acceso" className="text-2xl font-bold text-brand-navy">
        {texto("prefactura.titulo")}
      </h1>
      {mensaje !== null && (
        <Alert status={error === null ? "default" : "danger"} role={error === null ? "status" : "alert"} className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto(mensaje)}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
      <Form onSubmit={enviar} className="flex flex-col gap-5">
        <CampoObligatorio nombre="identificacion" etiqueta={texto("acceso.campo-identificacion")} mensajeVacio={texto("acceso.dato-obligatorio")} />
        <CampoObligatorio
          nombre="idAcceso"
          etiqueta={texto("acceso.campo-id-acceso")}
          ayuda={texto("acceso.ayuda")}
          mensajeVacio={texto("acceso.dato-obligatorio")}
        />
        <Button type="submit" size="lg" isPending={enviando} className="w-full sm:w-auto sm:self-start">
          {texto("acceso.boton")}
        </Button>
      </Form>
    </section>
  );
}
