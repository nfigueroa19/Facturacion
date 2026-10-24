import { useCallback, useEffect, useState } from "react";
import type { ClienteFacturador } from "./api/clienteFacturador";
import type { ClientePaciente } from "./api/clientePaciente";
import type { ClientePersonal } from "./api/clientePersonal";
import { lectorDe, useContenido } from "./contenido/ContenidoContexto";
import { EsqueletoAcceso } from "./componentes/EsqueletoAcceso";
import { ErrorContenido } from "./componentes/ErrorContenido";
import { Marco, type Superficie } from "./componentes/Marco";
import { AppPaciente } from "./pantallas/paciente/AppPaciente";
import { AppPersonal } from "./pantallas/personal/AppPersonal";

export interface Clientes {
  readonly paciente: ClientePaciente;
  readonly facturador: ClienteFacturador;
  readonly personal: ClientePersonal;
}

const RUTAS_PERSONAL = { "/facturador": "facturador", "/callcenter": "callcenter", "/digiturno": "digiturno" } as const;

/** Cada actor del personal tiene su ruta; cualquier otra es la web del paciente. */
export const superficieDe = (ruta: string): Superficie =>
  Object.entries(RUTAS_PERSONAL).find(([prefijo]) => ruta.startsWith(prefijo))?.[1] ?? "paciente";

interface PropsPantalla {
  readonly clientes: Clientes;
  readonly superficie: Superficie;
  readonly navegar: (ruta: string) => void;
}

function PantallaSegunContenido({ clientes, superficie, navegar }: PropsPantalla) {
  const { estado, reintentar } = useContenido();
  switch (estado.tipo) {
    case "cargando":
      return <EsqueletoAcceso />;
    case "error":
      return <ErrorContenido onReintentar={reintentar} />;
    case "listo": {
      const texto = lectorDe(estado.textos);
      return superficie === "paciente" ? (
        <AppPaciente cliente={clientes.paciente} texto={texto} />
      ) : (
        <AppPersonal clientes={clientes} superficie={superficie} texto={texto} navegar={navegar} />
      );
    }
  }
}

/**
 * Entre las rutas del personal se navega sin recargar la página: la sesión vive solo en memoria y una recarga
 * obligaría a ingresar de nuevo.
 */
export function App({ clientes, ruta }: { clientes: Clientes; ruta: string }) {
  const [rutaActual, setRutaActual] = useState(ruta);
  const navegar = useCallback((destino: string) => {
    window.history.pushState(null, "", destino);
    setRutaActual(destino);
  }, []);

  useEffect(() => {
    const alVolver = () => setRutaActual(window.location.pathname);
    window.addEventListener("popstate", alVolver);
    return () => window.removeEventListener("popstate", alVolver);
  }, []);

  const superficie = superficieDe(rutaActual);
  return (
    <Marco superficie={superficie}>
      <PantallaSegunContenido clientes={clientes} superficie={superficie} navegar={navegar} />
    </Marco>
  );
}
