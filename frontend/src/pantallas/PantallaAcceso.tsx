import { Alert, Button, Form } from "@heroui/react";
import { useState, type FormEvent } from "react";
import { textoDe } from "../contenido/ContenidoContexto";
import type { Textos } from "../contenido/clienteContenido";
import { CampoObligatorio } from "../componentes/CampoObligatorio";

export function PantallaAcceso({ textos }: { textos: Textos }) {
  const texto = (clave: string) => textoDe(textos, clave);
  const [enviado, setEnviado] = useState(false);

  // TODO: autenticar contra el backend cuando exista el endpoint de acceso (identificación + ID de acceso).
  const enviar = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    setEnviado(true);
  };

  return (
    <section aria-labelledby="titulo-acceso" className="aparicion-estado flex flex-col gap-6">
      <h1 id="titulo-acceso" className="text-2xl font-bold text-brand-navy">
        {texto("prefactura.titulo")}
      </h1>
      <Form onSubmit={enviar} className="flex flex-col gap-5">
        <CampoObligatorio nombre="identificacion" etiqueta={texto("acceso.campo-identificacion")} mensajeVacio={texto("acceso.dato-obligatorio")} />
        <CampoObligatorio
          nombre="idAcceso"
          etiqueta={texto("acceso.campo-id-acceso")}
          ayuda={texto("acceso.ayuda")}
          mensajeVacio={texto("acceso.dato-obligatorio")}
        />
        <Button type="submit" size="lg" className="w-full sm:w-auto sm:self-start">
          {texto("acceso.boton")}
        </Button>
      </Form>
      {enviado && (
        <Alert role="status" className="aparicion-estado">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{texto("acceso.servicio-pendiente")}</Alert.Description>
          </Alert.Content>
        </Alert>
      )}
    </section>
  );
}
