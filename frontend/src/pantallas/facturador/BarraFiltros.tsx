import { Button, Input, Label, TextField } from "@heroui/react";
import { ESTADOS, type EstadoPrefactura } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { FILTROS_INICIALES, type Filtros } from "./filtros";

const TODOS = "TODOS";

interface Props {
  readonly filtros: Filtros;
  readonly texto: Texto;
  readonly onCambiar: (filtros: Filtros) => void;
}

export function BarraFiltros({ filtros, texto, onCambiar }: Props) {
  const cambiados = filtros.estado !== FILTROS_INICIALES.estado || filtros.texto !== "" || filtros.fechaCita !== "";
  return (
    <div role="search" className="flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-end">
      <div className="flex flex-col gap-1">
        <label htmlFor="filtro-estado" className="text-sm font-medium text-ink">
          {texto("solicitudes.filtro-estado")}
        </label>
        <select
          id="filtro-estado"
          value={filtros.estado ?? TODOS}
          onChange={(evento) => {
            const valor = evento.currentTarget.value;
            onCambiar({ ...filtros, estado: valor === TODOS ? null : (valor as EstadoPrefactura) });
          }}
          className="min-h-10 rounded-[6px] border border-border-control bg-surface px-3 text-sm text-ink focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring"
        >
          <option value={TODOS}>{texto("solicitudes.todos")}</option>
          {ESTADOS.map((estado) => (
            <option key={estado} value={estado}>
              {texto(`estado.${estado}`)}
            </option>
          ))}
        </select>
      </div>
      <TextField value={filtros.texto} onChange={(valor) => onCambiar({ ...filtros, texto: valor })} className="flex flex-col gap-1 sm:w-64">
        <Label className="text-sm font-medium text-ink">{texto("solicitudes.filtro-texto")}</Label>
        <Input inputMode="search" className="min-h-10 text-sm" />
      </TextField>
      <TextField type="date" value={filtros.fechaCita} onChange={(valor) => onCambiar({ ...filtros, fechaCita: valor })} className="flex flex-col gap-1">
        <Label className="text-sm font-medium text-ink">{texto("solicitudes.filtro-fecha")}</Label>
        <Input className="min-h-10 text-sm" />
      </TextField>
      {cambiados && (
        <Button variant="ghost" onPress={() => onCambiar(FILTROS_INICIALES)} className="self-start sm:self-end">
          {texto("solicitudes.limpiar")}
        </Button>
      )}
    </div>
  );
}
