import { Button, Checkbox, CheckboxGroup, Description, Label, TextArea, TextField } from "@heroui/react";
import { useState } from "react";
import type { DetalleSolicitud, ItemRevision } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { AccionesDiaDeLaCita, type AccionesDeAtencion } from "./AccionesAtencion";

interface Props {
  readonly detalle: DetalleSolicitud;
  readonly texto: Texto;
  readonly aprobar: (revisados: readonly ItemRevision[]) => Promise<void>;
  readonly registrarNovedad: (descripcion: string) => Promise<void>;
  readonly confirmarLlegada: () => Promise<void>;
  readonly atencion: AccionesDeAtencion;
}

/** Acciones del diagrama del facturador según el estado actual de la solicitud. */
export function AccionesSolicitud({ detalle, texto, aprobar, registrarNovedad, confirmarLlegada, atencion }: Props) {
  const estado = detalle.resumen.estado;
  if (estado === "EN_REVISION") return <Revision detalle={detalle} texto={texto} aprobar={aprobar} registrarNovedad={registrarNovedad} />;
  if (estado === "APROBADO" || estado === "LISTO_PARA_FACTURAR") {
    return (
      <div className="flex flex-col gap-5">
        {estado === "APROBADO" && <Llegada texto={texto} confirmarLlegada={confirmarLlegada} />}
        <AccionesDiaDeLaCita detalle={detalle} texto={texto} acciones={atencion} />
      </div>
    );
  }
  return <p className="text-sm text-ink">{texto(`acciones.sin-acciones.${estado}`)}</p>;
}

function Revision({ detalle, texto, aprobar, registrarNovedad }: Pick<Props, "detalle" | "texto" | "aprobar" | "registrarNovedad">) {
  const [revisados, setRevisados] = useState<readonly string[]>([]);
  const [novedad, setNovedad] = useState("");
  const [error, setError] = useState<"checklist" | "novedad" | null>(null);
  const [ocupado, setOcupado] = useState<"aprobar" | "novedad" | null>(null);

  const alAprobar = async () => {
    if (detalle.checklist.some((item) => !revisados.includes(item))) return setError("checklist");
    setError(null);
    setOcupado("aprobar");
    await aprobar(detalle.checklist);
    setOcupado(null);
  };

  const alRegistrar = async () => {
    if (novedad.trim() === "") return setError("novedad");
    setError(null);
    setOcupado("novedad");
    await registrarNovedad(novedad);
    setOcupado(null);
  };

  return (
    <div className="flex flex-col gap-4">
      <CheckboxGroup
        value={[...revisados]}
        onChange={(valores) => {
          setRevisados(valores);
          if (error === "checklist") setError(null);
        }}
        className="flex flex-col gap-2"
      >
        <Label className="text-sm font-medium text-ink">{texto("checklist.titulo")}</Label>
        {detalle.checklist.map((item) => (
          <Checkbox key={item} value={item}>
            <Checkbox.Content className="text-sm text-ink">
              <Checkbox.Control>
                <Checkbox.Indicator />
              </Checkbox.Control>
              <Label>{texto(`checklist.${item}`)}</Label>
            </Checkbox.Content>
          </Checkbox>
        ))}
      </CheckboxGroup>
      {error === "checklist" && (
        <p role="alert" className="text-sm font-medium text-danger-fg">
          {texto("acciones.checklist-incompleto")}
        </p>
      )}
      <Button className="self-start" isPending={ocupado === "aprobar"} isDisabled={ocupado === "novedad"} onPress={() => void alAprobar()}>
        {texto("acciones.aprobar")}
      </Button>
      <TextField value={novedad} onChange={setNovedad} className="flex flex-col gap-1 border-t border-border pt-4">
        <Label className="text-sm font-medium text-ink">{texto("acciones.novedad")}</Label>
        <TextArea rows={3} maxLength={1000} className="text-sm" />
        <Description className="text-sm text-ink-subtle">{texto("acciones.novedad-ayuda")}</Description>
      </TextField>
      {error === "novedad" && (
        <p role="alert" className="text-sm font-medium text-danger-fg">
          {texto("acciones.novedad-vacia")}
        </p>
      )}
      <Button variant="outline" className="self-start" isPending={ocupado === "novedad"} isDisabled={ocupado === "aprobar"} onPress={() => void alRegistrar()}>
        {texto("acciones.registrar-novedad")}
      </Button>
    </div>
  );
}

function Llegada({ texto, confirmarLlegada }: Pick<Props, "texto" | "confirmarLlegada">) {
  const [ocupado, setOcupado] = useState(false);
  return (
    <div className="flex flex-col gap-3">
      <p className="text-sm text-ink">{texto("acciones.llegada-ayuda")}</p>
      <Button
        className="self-start"
        isPending={ocupado}
        onPress={() => {
          setOcupado(true);
          void confirmarLlegada().finally(() => setOcupado(false));
        }}
      >
        {texto("acciones.llegada")}
      </Button>
    </div>
  );
}
