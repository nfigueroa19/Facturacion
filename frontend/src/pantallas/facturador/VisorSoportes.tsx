import { Button } from "@heroui/react";
import { useEffect, useState } from "react";
import { TIPOS_SOPORTE, type TipoSoporte } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";

interface Props {
  readonly consecutivo: string;
  readonly cargados: readonly TipoSoporte[];
  readonly texto: Texto;
  readonly pedir: (tipo: TipoSoporte, senal: AbortSignal) => Promise<Blob>;
}

type Vista = { readonly tipo: "cargando" } | { readonly tipo: "error" } | { readonly tipo: "lista"; readonly url: string };

/**
 * El PDF se pide con el token y se muestra desde una URL `blob:` local: un iframe no puede mandar el encabezado
 * Authorization, y así el archivo nunca queda en una URL que se pueda compartir.
 */
export function VisorSoportes({ consecutivo, cargados, texto, pedir }: Props) {
  const [elegido, setElegido] = useState<TipoSoporte | null>(cargados[0] ?? null);
  const [vista, setVista] = useState<Vista>({ tipo: "cargando" });

  useEffect(() => {
    if (elegido === null) return;
    const control = new AbortController();
    let url: string | null = null;
    setVista({ tipo: "cargando" });
    pedir(elegido, control.signal)
      .then((archivo) => {
        url = URL.createObjectURL(new Blob([archivo], { type: "application/pdf" }));
        setVista({ tipo: "lista", url });
      })
      .catch(() => {
        if (!control.signal.aborted) setVista({ tipo: "error" });
      });
    return () => {
      control.abort();
      if (url !== null) URL.revokeObjectURL(url);
    };
    // `pedir` cambia en cada render; el PDF solo se vuelve a pedir al elegir otro soporte.
  }, [elegido]);

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-wrap items-center gap-2" role="group" aria-label={texto("detalle.soportes")}>
        {TIPOS_SOPORTE.map((tipo) =>
          cargados.includes(tipo) ? (
            <Button key={tipo} size="sm" variant={tipo === elegido ? "primary" : "outline"} aria-pressed={tipo === elegido} onPress={() => setElegido(tipo)}>
              {texto(`soportes.${tipo}`)}
            </Button>
          ) : (
            <span key={tipo} className="text-sm text-danger-fg">
              {texto(`soportes.${tipo}`)}: {texto("detalle.sin-soporte")}
            </span>
          ),
        )}
      </div>
      {elegido !== null && vista.tipo === "cargando" && <p className="text-sm text-ink-muted" role="status">{texto("detalle.cargando-pdf")}</p>}
      {elegido !== null && vista.tipo === "error" && <p className="text-sm font-medium text-danger-fg" role="alert">{texto("detalle.error-pdf")}</p>}
      {elegido !== null && vista.tipo === "lista" && (
        <>
          <iframe
            title={`${texto(`soportes.${elegido}`)} ${consecutivo}`}
            src={vista.url}
            className="h-[480px] w-full rounded-[6px] border border-border"
          />
          <a
            href={vista.url}
            download={`${consecutivo}-${elegido.toLowerCase().replace("_", "-")}.pdf`}
            className="self-start text-sm font-medium text-action underline underline-offset-2 hover:text-action-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring"
          >
            {texto("detalle.descargar")}
          </a>
        </>
      )}
    </div>
  );
}
