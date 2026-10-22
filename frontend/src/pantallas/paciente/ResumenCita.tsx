import type { Cita, Pago } from "../../api/tipos";
import { textoCopago } from "../../componentes/textoCopago";
import type { Texto } from "../../contenido/ContenidoContexto";
import { fechaLarga } from "../../formato";

export function ResumenCita({ cita, pago, texto }: { cita: Cita; pago: Pago | null; texto: Texto }) {
  const filas: readonly [string, string][] = [
    ["cita.fecha", fechaLarga(cita.fecha)],
    ["cita.medico", cita.medico],
    ["cita.especialidad", cita.especialidad],
    ["cita.regimen", texto(`regimen.${cita.regimen}`)],
    ["cita.copago", textoCopago(cita, pago, texto)],
  ];
  return (
    <section aria-labelledby="titulo-cita" className="rounded-[6px] border border-border p-4">
      <h2 id="titulo-cita" className="text-lg font-bold text-ink">
        {texto("cita.titulo")}
      </h2>
      <dl className="mt-3 grid grid-cols-1 gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
        {filas.map(([clave, valor]) => (
          <div key={clave} className="contents">
            <dt className="text-sm font-medium text-ink-muted sm:text-base">{texto(clave)}</dt>
            <dd className="text-base text-ink">{valor}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}
