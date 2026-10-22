import type { Cita, Pago } from "../api/tipos";
import type { Texto } from "../contenido/ContenidoContexto";
import { conValores, pesos } from "../formato";

/** Estado del copago de una cita, igual para el paciente y para el facturador. */
export function textoCopago(cita: Cita, pago: Pago | null, texto: Texto): string {
  if (cita.copagoPesos === null) return texto("copago.no-aplica");
  if (pago !== null) return conValores(texto("copago.estado-pagado"), { valor: pesos(pago.valorPesos), referencia: pago.referencia });
  return conValores(texto("copago.estado-ventanilla"), { valor: pesos(cita.copagoPesos) });
}
