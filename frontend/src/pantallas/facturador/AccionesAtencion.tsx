import { Button, Description, Input, Label, TextArea, TextField } from "@heroui/react";
import { useState } from "react";
import type { Atencion, DetalleSolicitud } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { fechaHora } from "../../formato";

/** Pasos del diagrama del facturador después de la aprobación. Dinámica no está conectada: los números se copian de allá. */
export interface AccionesDeAtencion {
  readonly registrarIngreso: (numero: string) => Promise<void>;
  readonly anularIngreso: (motivo: string) => Promise<void>;
  readonly registrarIntentoFallido: (error: string) => Promise<void>;
  readonly registrarFactura: (numero: string) => Promise<void>;
}

interface PropsFormulario {
  readonly clave: string;
  readonly texto: Texto;
  readonly multilinea?: boolean;
  readonly secundario?: boolean;
  readonly onEnviar: (valor: string) => Promise<void>;
}

/** Un dato y un botón. `clave` arma los textos: `<clave>.etiqueta`, `<clave>.ayuda` y `<clave>.boton`. */
function FormularioRegistro({ clave, texto, multilinea = false, secundario = false, onEnviar }: PropsFormulario) {
  const [valor, setValor] = useState("");
  const [vacio, setVacio] = useState(false);
  const [ocupado, setOcupado] = useState(false);

  const enviar = async () => {
    if (valor.trim() === "") return setVacio(true);
    setVacio(false);
    setOcupado(true);
    await onEnviar(valor.trim());
    setOcupado(false);
  };

  return (
    <div className="flex flex-col gap-2 border-t border-border pt-4 first:border-t-0 first:pt-0">
      <TextField value={valor} onChange={setValor} isInvalid={vacio} className="flex flex-col gap-1">
        <Label className="text-sm font-medium text-ink">{texto(`${clave}.etiqueta`)}</Label>
        {multilinea ? <TextArea rows={2} maxLength={1000} className="text-sm" /> : <Input maxLength={30} className="min-h-10 text-sm" />}
        <Description className="text-sm text-ink-subtle">{texto(`${clave}.ayuda`)}</Description>
      </TextField>
      {vacio && (
        <p role="alert" className="text-sm font-medium text-danger-fg">
          {texto("acciones.dato-vacio")}
        </p>
      )}
      <Button variant={secundario ? "outline" : "primary"} className="self-start" isPending={ocupado} onPress={() => void enviar()}>
        {texto(`${clave}.boton`)}
      </Button>
    </div>
  );
}

/** Formularios que aplican según el estado y lo que ya se registró en la atención. */
export function AccionesDiaDeLaCita({ detalle, texto, acciones }: { detalle: DetalleSolicitud; texto: Texto; acciones: AccionesDeAtencion }) {
  const { estado } = detalle.resumen;
  const conIngreso = detalle.atencion.ingreso !== null;
  return (
    <div className="flex flex-col gap-4">
      <p className="text-sm text-ink-muted">{texto("acciones.dia-de-la-cita")}</p>
      {!conIngreso && <FormularioRegistro clave="acciones.ingreso" texto={texto} onEnviar={acciones.registrarIngreso} />}
      {estado === "LISTO_PARA_FACTURAR" && conIngreso && (
        <>
          <FormularioRegistro clave="acciones.factura" texto={texto} onEnviar={acciones.registrarFactura} />
          <FormularioRegistro clave="acciones.intento" texto={texto} multilinea secundario onEnviar={acciones.registrarIntentoFallido} />
        </>
      )}
      {estado === "APROBADO" && conIngreso && (
        <FormularioRegistro clave="acciones.anulacion" texto={texto} multilinea secundario onEnviar={acciones.anularIngreso} />
      )}
    </div>
  );
}

/** Ingreso, intentos fallidos, factura y anulación registrados, en el orden en que ocurren. */
export function HistorialAtencion({ atencion, texto }: { atencion: Atencion; texto: Texto }) {
  const filas = [
    ...(atencion.ingreso === null ? [] : [{ clave: "atencion.ingreso", registro: atencion.ingreso }]),
    ...atencion.intentosFallidos.map((registro) => ({ clave: "atencion.intento", registro })),
    ...(atencion.factura === null ? [] : [{ clave: "atencion.factura", registro: atencion.factura }]),
    ...(atencion.anulacion === null ? [] : [{ clave: "atencion.anulacion", registro: atencion.anulacion }]),
  ];
  if (filas.length === 0) return <p className="text-sm text-ink-muted">{texto("atencion.vacia")}</p>;
  return (
    <ul className="flex flex-col gap-2 text-sm">
      {filas.map(({ clave, registro }) => (
        <li key={`${clave}-${registro.fecha}`} className="rounded-[6px] border border-border p-2 text-ink">
          <span className="font-medium">{texto(clave)}:</span> {registro.valor}
          <span className="mt-0.5 block text-ink-subtle">
            {registro.facturador}, {fechaHora(registro.fecha)}
          </span>
        </li>
      ))}
    </ul>
  );
}
