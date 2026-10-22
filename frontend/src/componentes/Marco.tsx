import type { ReactNode } from "react";
import { textoDe, useContenido } from "../contenido/ContenidoContexto";
import { LogoInstitucional } from "./LogoInstitucional";

const NOMBRE_RESPALDO = "Hospital Susana López de Valencia";

export function Marco({ children }: { children: ReactNode }) {
  const { estado } = useContenido();
  const nombre = estado.tipo === "listo" ? textoDe(estado.textos, "institucion.nombre") : NOMBRE_RESPALDO;
  return (
    <div className="flex min-h-dvh flex-col">
      <header className="bg-surface">
        <div className="mx-auto flex max-w-[640px] items-center px-4 py-3 sm:py-4">
          {/* La clave por estado vuelve a pedir el logo tras un reintento, si antes había fallado. */}
          <LogoInstitucional key={estado.tipo} clave="logo-encabezado" alternativo={nombre} className="h-auto w-28 sm:w-40" />
        </div>
        <div className="franja-identidad" />
      </header>
      <main className="mx-auto w-full max-w-[640px] flex-1 px-4 py-8 sm:py-12">{children}</main>
      <footer className="bg-brand-navy text-white">
        <div className="mx-auto flex max-w-[640px] items-center px-4 py-4">
          <LogoInstitucional key={estado.tipo} clave="logo-pie-pagina" alternativo={nombre} className="h-auto w-28" />
        </div>
      </footer>
    </div>
  );
}
