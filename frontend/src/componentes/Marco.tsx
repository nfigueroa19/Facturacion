import type { ReactNode } from "react";
import { textoDe, useContenido } from "../contenido/ContenidoContexto";
import { LogoInstitucional } from "./LogoInstitucional";

const NOMBRE_RESPALDO = "Hospital Susana López de Valencia";

export type Superficie = "paciente" | "facturador" | "callcenter" | "digiturno";

/** Paciente: una columna de 640 px (DESIGN.md 5.2). Personal: barra azul y ancho de herramienta (5.3). */
export function Marco({ superficie, children }: { superficie: Superficie; children: ReactNode }) {
  const { estado } = useContenido();
  const nombre = estado.tipo === "listo" ? textoDe(estado.textos, "institucion.nombre") : NOMBRE_RESPALDO;
  const personal = superficie !== "paciente";
  const ancho = personal ? "max-w-[1440px]" : "max-w-[640px]";
  return (
    <div className="flex min-h-dvh flex-col">
      <header className={personal ? "bg-brand-navy text-white" : "bg-surface"}>
        <div className={`mx-auto flex ${ancho} items-center justify-between gap-4 px-4 py-3 sm:py-4`}>
          {/* La clave por estado vuelve a pedir el logo tras un reintento, si antes había fallado. */}
          <LogoInstitucional
            key={estado.tipo}
            clave={personal ? "logo-pie-pagina" : "logo-encabezado"}
            alternativo={nombre}
            className="h-auto w-28 sm:w-40"
          />
          {personal && estado.tipo === "listo" && (
            <span className="text-sm font-medium">{textoDe(estado.textos, `${superficie}.panel`)}</span>
          )}
        </div>
        <div className="franja-identidad" />
      </header>
      <main className={`mx-auto w-full ${ancho} flex-1 px-4 ${personal ? "py-6" : "py-8 sm:py-12"}`}>{children}</main>
      {!personal && (
        <footer className="bg-brand-navy text-white">
          <div className="mx-auto flex max-w-[640px] items-center px-4 py-4">
            <LogoInstitucional key={estado.tipo} clave="logo-pie-pagina" alternativo={nombre} className="h-auto w-28" />
          </div>
        </footer>
      )}
    </div>
  );
}
