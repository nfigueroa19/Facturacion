import { Button } from "@heroui/react";
import { useEffect, useRef } from "react";
import type { Turno } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";

interface Props {
  readonly turno: Turno;
  readonly texto: Texto;
  readonly onSiguiente: () => void;
}

export function TurnoAsignado({ turno, texto, onSiguiente }: Props) {
  const titulo = useRef<HTMLParagraphElement>(null);
  const preconsulta = turno.destino === "PRECONSULTA";
  useEffect(() => titulo.current?.focus(), []);

  return (
    <div
      role="status"
      className={`aparicion-estado flex flex-col gap-4 rounded-[6px] border-l-4 p-5 ${
        preconsulta ? "border-action bg-state-aprobado-bg" : "border-state-pendiente-fg bg-state-pendiente-bg"
      }`}
    >
      <p className="text-sm font-medium text-ink-muted">{texto("digiturno.su-turno")}</p>
      <p ref={titulo} tabIndex={-1} className="text-5xl font-bold tabular-nums text-brand-navy outline-none">
        {turno.turno}
      </p>
      <p className="text-xl font-bold text-ink">{texto(`digiturno.destino.${turno.destino}`)}</p>
      {turno.motivo !== null && <p className="text-base text-ink">{texto(`digiturno.motivo.${turno.motivo}`)}</p>}
      {turno.novedades.length > 0 && (
        <section aria-labelledby="titulo-novedades-digiturno" className="flex flex-col gap-2">
          <h2 id="titulo-novedades-digiturno" className="text-base font-bold text-ink">
            {texto("digiturno.novedades")}
          </h2>
          <ul className="list-disc pl-5 text-base text-ink">
            {turno.novedades.map((novedad) => (
              <li key={novedad.fecha}>{novedad.descripcion}</li>
            ))}
          </ul>
        </section>
      )}
      <Button size="lg" className="self-start" onPress={onSiguiente}>
        {texto("digiturno.siguiente")}
      </Button>
    </div>
  );
}
