import { CircleCheck } from "lucide-react";
import { useId, useState, type ChangeEvent } from "react";
import type { ResultadoCarga } from "../../api/clientePaciente";
import { TIPOS_SOPORTE, type TipoSoporte } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores, megabytes } from "../../formato";
import { motivoRechazoPdf } from "./validacion";

type EstadoArchivo =
  | { readonly tipo: "quieto" }
  | { readonly tipo: "subiendo" }
  | { readonly tipo: "error"; readonly clave: string };

interface Props {
  readonly tamanoMaximo: number;
  readonly cargados: ReadonlySet<TipoSoporte>;
  readonly texto: Texto;
  /** Lanza un error si no hay conexión; la sesión vencida la resuelve quien la pasa. */
  readonly subir: (tipo: TipoSoporte, archivo: File) => Promise<ResultadoCarga>;
  readonly onCargado: (tipo: TipoSoporte) => void;
}

export function PasoSoportes(props: Props) {
  return (
    <div className="flex flex-col gap-4">
      <p className="text-base text-ink-muted">
        {props.texto("soportes.instruccion")} {conValores(props.texto("soportes.limite"), { tamano: megabytes(props.tamanoMaximo) })}
      </p>
      {TIPOS_SOPORTE.map((tipo) => (
        <TarjetaSoporte key={tipo} tipo={tipo} {...props} />
      ))}
    </div>
  );
}

function TarjetaSoporte({ tipo, tamanoMaximo, cargados, texto, subir, onCargado }: Props & { tipo: TipoSoporte }) {
  const [estado, setEstado] = useState<EstadoArchivo>({ tipo: "quieto" });
  const id = useId();
  const cargado = cargados.has(tipo);

  const elegir = async (evento: ChangeEvent<HTMLInputElement>) => {
    const archivo = evento.currentTarget.files?.[0];
    // Se limpia para que elegir de nuevo el mismo archivo vuelva a disparar la carga.
    evento.currentTarget.value = "";
    if (archivo === undefined) return;
    const motivo = await motivoRechazoPdf(archivo, tamanoMaximo);
    if (motivo !== null) return setEstado({ tipo: "error", clave: `soportes.error.${motivo}` });
    setEstado({ tipo: "subiendo" });
    try {
      const resultado = await subir(tipo, archivo);
      if (resultado.tipo === "rechazado") return setEstado({ tipo: "error", clave: `soportes.error.${resultado.motivo}` });
      setEstado({ tipo: "quieto" });
      onCargado(tipo);
    } catch {
      setEstado({ tipo: "error", clave: "soportes.error.conexion" });
    }
  };

  return (
    <div className="flex flex-col gap-3 rounded-[6px] border border-border p-4">
      <h2 id={`${id}-titulo`} className="text-lg font-bold text-ink">
        {texto(`soportes.${tipo}`)}
      </h2>
      <input
        type="file"
        accept="application/pdf,.pdf"
        aria-labelledby={`${id}-titulo`}
        aria-describedby={`${id}-estado`}
        disabled={estado.tipo === "subiendo"}
        onChange={(evento) => void elegir(evento)}
        className="min-h-11 w-full text-base text-ink file:mr-3 file:min-h-11 file:cursor-pointer file:rounded-[6px] file:border file:border-solid file:border-border-control file:bg-surface file:px-4 file:font-medium file:text-ink hover:file:bg-surface-2 disabled:opacity-60"
      />
      <EstadoTarjeta id={`${id}-estado`} estado={estado} cargado={cargado} texto={texto} tamanoMaximo={tamanoMaximo} />
    </div>
  );
}

function EstadoTarjeta({ id, estado, cargado, texto, tamanoMaximo }: { id: string; estado: EstadoArchivo; cargado: boolean; texto: Texto; tamanoMaximo: number }) {
  const contenido = (() => {
    if (estado.tipo === "subiendo") return <span className="text-ink-muted">{texto("soportes.estado.subiendo")}</span>;
    if (estado.tipo === "error")
      return <span className="font-medium text-danger-fg">{conValores(texto(estado.clave), { tamano: megabytes(tamanoMaximo) })}</span>;
    if (!cargado) return <span className="text-ink-muted">{texto("soportes.estado.pendiente")}</span>;
    return (
      <span className="inline-flex items-center gap-1.5 font-medium text-action">
        <CircleCheck aria-hidden="true" className="size-5" />
        {texto("soportes.estado.cargado")}
      </span>
    );
  })();
  return (
    <p id={id} role="status" className="text-base">
      {contenido}
    </p>
  );
}
