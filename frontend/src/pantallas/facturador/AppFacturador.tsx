import { useCallback, useState } from "react";
import type { ClienteFacturador, SesionFacturador } from "../../api/clienteFacturador";
import type { Texto } from "../../contenido/ContenidoContexto";
import { AccesoFacturador } from "./AccesoFacturador";
import { PanelSolicitudes } from "./PanelSolicitudes";

/** La sesión vive solo en memoria: al recargar la página el facturador ingresa de nuevo. */
export function AppFacturador({ cliente, texto }: { cliente: ClienteFacturador; texto: Texto }) {
  const [sesion, setSesion] = useState<SesionFacturador | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const salir = useCallback((motivo: string | null) => {
    setSesion(null);
    setAviso(motivo);
  }, []);

  if (sesion === null) {
    return (
      <AccesoFacturador
        texto={texto}
        aviso={aviso}
        iniciarSesion={(usuario, clave) => cliente.iniciarSesion(usuario, clave)}
        onIngreso={(nueva) => {
          setAviso(null);
          setSesion(nueva);
        }}
      />
    );
  }
  return <PanelSolicitudes cliente={cliente} sesion={sesion} texto={texto} onSalir={salir} />;
}
