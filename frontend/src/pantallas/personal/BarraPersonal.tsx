import { Button } from "@heroui/react";
import type { SesionFacturador } from "../../api/clienteFacturador";
import type { ClientePersonal } from "../../api/clientePersonal";
import type { ClientePruebas } from "../../api/clientePruebas";
import type { Superficie } from "../../componentes/Marco";
import type { Texto } from "../../contenido/ContenidoContexto";
import { PanelComunicaciones } from "./PanelComunicaciones";
import { RestablecerEscenarios } from "./RestablecerEscenarios";

const SECCIONES = [
  { superficie: "facturador", ruta: "/facturador" },
  { superficie: "callcenter", ruta: "/callcenter" },
  { superficie: "digiturno", ruta: "/digiturno" },
] as const;

interface Props {
  readonly superficie: Exclude<Superficie, "paciente">;
  readonly sesion: SesionFacturador;
  readonly cliente: ClientePersonal;
  /** Solo cuando el backend publica el catálogo de pruebas: entonces se pueden restablecer. */
  readonly pruebas: ClientePruebas | null;
  readonly texto: Texto;
  readonly navegar: (ruta: string) => void;
  readonly onSalir: (motivo: string | null) => void;
}

/** Navegación entre los módulos del personal, panel de comunicaciones, restablecimiento de los datos de prueba y salida. */
export function BarraPersonal({ superficie, sesion, cliente, pruebas, texto, navegar, onSalir }: Props) {
  const vencida = () => onSalir("facturador.acceso.sesion-vencida");
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border pb-3">
      <nav aria-label={texto("personal.navegacion")}>
        <ul className="flex flex-wrap gap-1">
          {SECCIONES.map((seccion) => (
            <li key={seccion.ruta}>
              <a
                href={seccion.ruta}
                aria-current={seccion.superficie === superficie ? "page" : undefined}
                onClick={(evento) => {
                  evento.preventDefault();
                  navegar(seccion.ruta);
                }}
                className="inline-flex min-h-9 items-center rounded-[6px] px-3 text-sm font-medium text-ink-muted transition-colors duration-200 hover:bg-surface-secondary hover:text-ink focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring aria-[current=page]:bg-state-listo-bg aria-[current=page]:text-brand-navy"
              >
                {texto(`${seccion.superficie}.panel`)}
              </a>
            </li>
          ))}
        </ul>
      </nav>
      <div className="flex flex-wrap items-center gap-3 text-sm text-ink-muted">
        <span>{sesion.nombre}</span>
        <PanelComunicaciones cliente={cliente} token={sesion.token} texto={texto} onSesionVencida={vencida} />
        {pruebas !== null && <RestablecerEscenarios cliente={pruebas} token={sesion.token} texto={texto} onSesionVencida={vencida} />}
        <Button variant="outline" size="sm" onPress={() => onSalir(null)}>
          {texto("facturador.salir")}
        </Button>
      </div>
    </div>
  );
}
