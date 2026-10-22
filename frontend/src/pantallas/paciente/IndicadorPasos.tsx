import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores } from "../../formato";

export type Paso = "datos" | "soportes" | "copago" | "confirmacion";

export function IndicadorPasos({ pasos, actual, texto }: { pasos: readonly Paso[]; actual: number; texto: Texto }) {
  return (
    <nav aria-label={texto("pasos.etiqueta")}>
      <p className="text-sm font-medium text-ink-muted">{conValores(texto("pasos.progreso"), { actual: actual + 1, total: pasos.length })}</p>
      <ol className="mt-2 grid gap-2" style={{ gridTemplateColumns: `repeat(${pasos.length}, minmax(0, 1fr))` }}>
        {pasos.map((paso, i) => (
          <li key={paso} aria-current={i === actual ? "step" : undefined} className="flex flex-col gap-1">
            {/* La barra acompaña al texto; el progreso se lee en "Paso n de m" y en el título. */}
            <span aria-hidden="true" className={`h-1 rounded-full ${i <= actual ? "bg-action" : "bg-border"}`} />
            <span className={`hidden text-sm sm:block ${i === actual ? "font-bold text-ink" : "text-ink-subtle"}`}>{texto(`pasos.${paso}`)}</span>
          </li>
        ))}
      </ol>
    </nav>
  );
}
