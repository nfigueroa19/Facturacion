import { Ban, CircleCheck, ClipboardCheck, Clock, Receipt, TriangleAlert, type LucideIcon } from "lucide-react";
import type { EstadoPrefactura } from "../api/tipos";

// Colores de DESIGN.md, sección 2.3. El estado siempre lleva texto e icono, nunca solo color.
const APARIENCIA: Readonly<Record<EstadoPrefactura, { clases: string; Icono: LucideIcon }>> = {
  EN_REVISION: { clases: "bg-state-radicado-bg text-state-radicado-fg", Icono: Clock },
  PENDIENTE: { clases: "bg-state-pendiente-bg text-state-pendiente-fg", Icono: TriangleAlert },
  APROBADO: { clases: "bg-state-aprobado-bg text-state-aprobado-fg", Icono: CircleCheck },
  LISTO_PARA_FACTURAR: { clases: "border border-state-listo-fg bg-state-listo-bg text-state-listo-fg", Icono: ClipboardCheck },
  FACTURADO: { clases: "bg-state-facturado-bg text-state-facturado-fg", Icono: Receipt },
  INGRESO_ANULADO: { clases: "bg-state-anulado-bg text-state-anulado-fg", Icono: Ban },
};

export function EstadoSolicitud({ estado, texto }: { estado: EstadoPrefactura; texto: (clave: string) => string }) {
  const { clases, Icono } = APARIENCIA[estado];
  return (
    <span className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full px-2.5 py-0.5 text-sm font-medium ${clases}`}>
      <Icono aria-hidden="true" className="size-4 shrink-0" strokeWidth={2} />
      {texto(`estado.${estado}`)}
    </span>
  );
}
