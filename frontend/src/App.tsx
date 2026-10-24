import { useCallback, useEffect, useState } from "react";
import type { ClienteFacturador } from "./api/clienteFacturador";
import type { ClientePaciente } from "./api/clientePaciente";
import type { ClientePersonal } from "./api/clientePersonal";
import type { ClientePruebas } from "./api/clientePruebas";
import type { PasarelaNavegador } from "./api/pasarelaWompi";
import type { CatalogoPruebas } from "./api/tipos";
import { lectorDe, useContenido } from "./contenido/ContenidoContexto";
import { EsqueletoAcceso } from "./componentes/EsqueletoAcceso";
import { ErrorContenido } from "./componentes/ErrorContenido";
import { Marco, type Superficie } from "./componentes/Marco";
import { AppPaciente } from "./pantallas/paciente/AppPaciente";
import { AppPersonal } from "./pantallas/personal/AppPersonal";
import { useCarga } from "./useCarga";

export interface Clientes {
  readonly paciente: ClientePaciente;
  readonly facturador: ClienteFacturador;
  readonly personal: ClientePersonal;
  readonly pruebas: ClientePruebas;
  readonly pasarela: PasarelaNavegador;
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

/** El catálogo de pruebas es opcional: si el backend no lo publica o no responde, las pantallas siguen sin él. */
function useCatalogoPruebas(cliente: ClientePruebas): CatalogoPruebas | null {
  const { carga } = useCarga((senal) => cliente.catalogo(senal), []);
  return carga.tipo === "lista" ? carga.valor : null;
}

function PantallaSegunContenido({ clientes, superficie, navegar }: PropsPantalla) {
  const { estado, reintentar } = useContenido();
  const catalogo = useCatalogoPruebas(clientes.pruebas);
  switch (estado.tipo) {
    case "cargando":
      return <EsqueletoAcceso />;
    case "error":
      return <ErrorContenido onReintentar={reintentar} />;
    case "listo": {
      const texto = lectorDe(estado.textos);
      return superficie === "paciente" ? (
        <AppPaciente cliente={clientes.paciente} pasarela={clientes.pasarela} texto={texto} catalogo={catalogo} />
      ) : (
        <AppPersonal clientes={clientes} superficie={superficie} texto={texto} navegar={navegar} catalogo={catalogo} />
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
