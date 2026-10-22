import type { ClienteFacturador } from "./api/clienteFacturador";
import type { ClientePaciente } from "./api/clientePaciente";
import { lectorDe, useContenido } from "./contenido/ContenidoContexto";
import { EsqueletoAcceso } from "./componentes/EsqueletoAcceso";
import { ErrorContenido } from "./componentes/ErrorContenido";
import { Marco, type Superficie } from "./componentes/Marco";
import { AppFacturador } from "./pantallas/facturador/AppFacturador";
import { AppPaciente } from "./pantallas/paciente/AppPaciente";

export interface Clientes {
  readonly paciente: ClientePaciente;
  readonly facturador: ClienteFacturador;
}

/** El panel vive en /facturador; cualquier otra ruta es la web del paciente. */
export const superficieDe = (ruta: string): Superficie => (ruta.startsWith("/facturador") ? "facturador" : "paciente");

function PantallaSegunContenido({ clientes, superficie }: { clientes: Clientes; superficie: Superficie }) {
  const { estado, reintentar } = useContenido();
  switch (estado.tipo) {
    case "cargando":
      return <EsqueletoAcceso />;
    case "error":
      return <ErrorContenido onReintentar={reintentar} />;
    case "listo": {
      const texto = lectorDe(estado.textos);
      return superficie === "facturador" ? (
        <AppFacturador cliente={clientes.facturador} texto={texto} />
      ) : (
        <AppPaciente cliente={clientes.paciente} texto={texto} />
      );
    }
  }
}

export function App({ clientes, ruta }: { clientes: Clientes; ruta: string }) {
  const superficie = superficieDe(ruta);
  return (
    <Marco superficie={superficie}>
      <PantallaSegunContenido clientes={clientes} superficie={superficie} />
    </Marco>
  );
}
