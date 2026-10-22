const PESOS = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });
const FECHA = new Intl.DateTimeFormat("es-CO", { dateStyle: "long" });
const FECHA_CORTA = new Intl.DateTimeFormat("es-CO", { day: "2-digit", month: "2-digit", year: "numeric" });
// Las fechas de envío vienen en UTC; el hospital opera en hora de Colombia.
const FECHA_HORA = new Intl.DateTimeFormat("es-CO", { dateStyle: "medium", timeStyle: "short", timeZone: "America/Bogota" });

export const pesos = (valor: number) => PESOS.format(valor);

// "2026-10-22" sin hora se interpretaría en UTC y podría mostrarse como el día anterior.
const diaLocal = (iso: string) => new Date(`${iso}T00:00:00`);

export const fechaLarga = (iso: string) => FECHA.format(diaLocal(iso));
export const fechaCorta = (iso: string) => FECHA_CORTA.format(diaLocal(iso));
export const fechaHora = (iso: string) => FECHA_HORA.format(new Date(iso));

export const megabytes = (bytes: number) => `${Math.floor(bytes / (1024 * 1024))} MB`;

/** Reemplaza `{nombre}` en un texto de contenido.json. */
export function conValores(texto: string, valores: Readonly<Record<string, string | number>>): string {
  return texto.replace(/\{(\w+)\}/g, (marca, nombre: string) => String(valores[nombre] ?? marca));
}
