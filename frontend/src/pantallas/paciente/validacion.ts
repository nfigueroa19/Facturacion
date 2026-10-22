import type { Campo, ErrorCampo, MotivoRechazoPdf } from "../../api/tipos";

// Copia de las reglas técnicas de TipoCampo.kt y CampoFormulario.kt para avisar antes de enviar.
// El backend vuelve a validar y su respuesta manda.
const LARGO_MAXIMO = 120;
const FORMATOS: Readonly<Record<Campo["tipo"], RegExp | null>> = {
  TEXTO: null,
  FECHA: /^\d{4}-\d{2}-\d{2}$/,
  CORREO: /^[^@\s]+@[^@\s]+\.[^@\s]+$/,
  TELEFONO: /^\+?[0-9][0-9 ]{5,18}[0-9]$/,
};

export function errorDeCampo(campo: Campo, valor: string): ErrorCampo | null {
  const limpio = valor.trim();
  if (limpio === "") return campo.obligatorio ? "FALTA" : null;
  if (limpio.length > LARGO_MAXIMO) return "LARGO";
  const formato = FORMATOS[campo.tipo];
  return formato === null || formato.test(limpio) ? null : "FORMATO";
}

const FIRMA_PDF = "%PDF-";

/** Revisa en el navegador lo mismo que DocumentoPdf.kt: tamaño y firma, sin confiar en la extensión. */
export async function motivoRechazoPdf(archivo: Blob, tamanoMaximo: number): Promise<MotivoRechazoPdf | null> {
  if (archivo.size === 0) return "VACIO";
  if (archivo.size > tamanoMaximo) return "EXCEDE_TAMANO";
  const inicio = await archivo.slice(0, FIRMA_PDF.length).text();
  return inicio === FIRMA_PDF ? null : "NO_ES_PDF";
}
