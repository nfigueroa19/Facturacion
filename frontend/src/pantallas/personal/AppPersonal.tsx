import { useCallback, useState } from "react";
import type { Clientes } from "../../App";
import type { SesionFacturador } from "../../api/clienteFacturador";
import type { CatalogoPruebas } from "../../api/tipos";
import { DatosPrueba } from "../../componentes/DatosPrueba";
import type { Superficie } from "../../componentes/Marco";
import type { Texto } from "../../contenido/ContenidoContexto";
import { AppCallCenter } from "../callcenter/AppCallCenter";
import { AppDigiturno } from "../digiturno/AppDigiturno";
import { AccesoFacturador } from "../facturador/AccesoFacturador";
import { PanelSolicitudes } from "../facturador/PanelSolicitudes";
import { BarraPersonal } from "./BarraPersonal";

interface Props {
  readonly clientes: Clientes;
  readonly superficie: Exclude<Superficie, "paciente">;
  readonly texto: Texto;
  readonly navegar: (ruta: string) => void;
  readonly catalogo: CatalogoPruebas | null;
}

/**
 * Facturador, call center y digiturno comparten una sesión del personal. Vive solo en memoria: al recargar la
 * página se ingresa de nuevo.
 */
export function AppPersonal({ clientes, superficie, texto, navegar, catalogo }: Props) {
  const [sesion, setSesion] = useState<SesionFacturador | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const salir = useCallback((motivo: string | null) => {
    setSesion(null);
    setAviso(motivo);
  }, []);

  if (sesion === null) {
    return (
      <div className="mx-auto flex w-full max-w-sm flex-col gap-8">
        <AccesoFacturador
          texto={texto}
          aviso={aviso}
          iniciarSesion={(usuario, clave) => clientes.facturador.iniciarSesion(usuario, clave)}
          onIngreso={(nueva) => {
            setAviso(null);
            setSesion(nueva);
          }}
        />
        {catalogo !== null && <DatosPrueba catalogo={catalogo} texto={texto} pestanaInicial="personal" />}
      </div>
    );
  }
  const props = { sesion, texto, onSalir: salir };
  return (
    <div className="flex flex-col gap-5">
      <BarraPersonal
        superficie={superficie}
        sesion={sesion}
        cliente={clientes.personal}
        pruebas={catalogo === null ? null : clientes.pruebas}
        texto={texto}
        navegar={navegar}
        onSalir={salir}
      />
      {superficie === "facturador" && <PanelSolicitudes cliente={clientes.facturador} {...props} />}
      {superficie === "callcenter" && <AppCallCenter cliente={clientes.personal} {...props} />}
      {superficie === "digiturno" && <AppDigiturno cliente={clientes.personal} {...props} />}
    </div>
  );
}
