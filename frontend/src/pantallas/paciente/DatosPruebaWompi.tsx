import { Button, Modal } from "@heroui/react";
import type { Texto } from "../../contenido/ContenidoContexto";

type Resultado = "APPROVED" | "DECLINED" | "ERROR";

interface DatoPrueba {
  /** null: cualquier otro valor, que Wompi sandbox responde con ERROR. */
  readonly valor: string | null;
  readonly resultado: Resultado;
}

// Datos de prueba publicados por Wompi en https://docs.wompi.co/docs/colombia/datos-de-prueba-en-sandbox/ (2026-10-24).
const TARJETAS: readonly DatoPrueba[] = [
  { valor: "4242 4242 4242 4242", resultado: "APPROVED" },
  { valor: "4111 1111 1111 1111", resultado: "DECLINED" },
  { valor: null, resultado: "ERROR" },
];

const NEQUI: readonly DatoPrueba[] = [
  { valor: "3991111111", resultado: "APPROVED" },
  { valor: "3992222222", resultado: "DECLINED" },
  { valor: null, resultado: "ERROR" },
];

/** El widget de Wompi tapa la página: el paciente consulta aquí los datos antes de abrirlo. */
export function DatosPruebaWompi({ texto }: { texto: Texto }) {
  return (
    <Modal>
      <Button variant="outline" className="min-h-11">
        {texto("copago.wompi.ayuda.abrir")}
      </Button>
      <Modal.Backdrop>
        <Modal.Container size="md" scroll="inside">
          <Modal.Dialog className="rounded-[6px]">
            <Modal.CloseTrigger aria-label={texto("copago.wompi.ayuda.cerrar")} />
            <Modal.Header>
              <Modal.Heading className="text-lg font-bold text-brand-navy">{texto("copago.wompi.ayuda.titulo")}</Modal.Heading>
              <p className="text-sm text-ink-muted">{texto("copago.wompi.ayuda.descripcion")}</p>
            </Modal.Header>
            <Modal.Body className="flex flex-col gap-6">
              <div className="flex flex-col gap-2">
                <TablaDatos titulo={texto("copago.wompi.ayuda.tarjetas")} otro={texto("copago.wompi.ayuda.otra-tarjeta")} datos={TARJETAS} texto={texto} />
                <p className="text-sm text-ink-muted">{texto("copago.wompi.ayuda.condiciones-tarjeta")}</p>
              </div>
              <TablaDatos titulo={texto("copago.wompi.ayuda.nequi")} otro={texto("copago.wompi.ayuda.otro-celular")} datos={NEQUI} texto={texto} />
              <p className="text-sm text-ink-muted">{texto("copago.wompi.ayuda.pse")}</p>
            </Modal.Body>
            <Modal.Footer>
              <Button slot="close" variant="outline" className="min-h-11">
                {texto("copago.wompi.ayuda.cerrar")}
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}

interface PropsTabla {
  readonly titulo: string;
  readonly otro: string;
  readonly datos: readonly DatoPrueba[];
  readonly texto: Texto;
}

function TablaDatos({ titulo, otro, datos, texto }: PropsTabla) {
  return (
    <table className="w-full text-left text-sm">
      <caption className="pb-2 text-left text-base font-bold text-ink">{titulo}</caption>
      <thead>
        <tr className="border-b border-border text-ink-subtle">
          <th scope="col" className="py-2 pr-4 font-medium">
            {texto("copago.wompi.ayuda.columna-dato")}
          </th>
          <th scope="col" className="py-2 font-medium">
            {texto("copago.wompi.ayuda.columna-resultado")}
          </th>
        </tr>
      </thead>
      <tbody className="divide-y divide-border">
        {datos.map(({ valor, resultado }) => (
          <tr key={valor ?? "otro"} className="h-10">
            <td className={valor === null ? "py-2 pr-4 text-ink-muted" : "py-2 pr-4 font-medium tabular-nums text-ink select-all"}>
              {valor ?? otro}
            </td>
            <td className="py-2 text-ink">{texto(`copago.wompi.resultado.${resultado}`)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
