import { Checkbox, Label } from "@heroui/react";
import type { CheckoutPago } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";

export interface Aceptacion {
  readonly terminos: boolean;
  readonly datos: boolean;
}

interface Props {
  readonly contratos: CheckoutPago["aceptacion"];
  readonly aceptacion: Aceptacion;
  /** true después de un intento de pago sin aceptar los dos documentos. */
  readonly faltaAceptar: boolean;
  readonly texto: Texto;
  readonly onCambiar: (aceptacion: Aceptacion) => void;
}

/**
 * Wompi exige que el pagador acepte por separado su reglamento y la autorización de datos personales antes de crear la
 * transacción. El enlace va fuera de la etiqueta para que abrir el PDF no marque la casilla.
 */
export function AceptacionWompi({ contratos, aceptacion, faltaAceptar, texto, onCambiar }: Props) {
  const casillas = [
    { clave: "terminos", enlace: contratos.terminos.enlace },
    { clave: "datos", enlace: contratos.datosPersonales.enlace },
  ] as const;
  return (
    <div className="flex flex-col gap-3">
      {casillas.map(({ clave, enlace }) => (
        <div key={clave} className="flex flex-col gap-1">
          <Checkbox
            isSelected={aceptacion[clave]}
            isInvalid={faltaAceptar && !aceptacion[clave]}
            onChange={(marcada) => onCambiar({ ...aceptacion, [clave]: marcada })}
            className="min-h-11"
          >
            <Checkbox.Content className="min-h-11 items-center text-base text-ink">
              <Checkbox.Control>
                <Checkbox.Indicator />
              </Checkbox.Control>
              <Label>{texto(`copago.wompi.aceptacion.${clave}`)}</Label>
            </Checkbox.Content>
          </Checkbox>
          <a href={enlace} target="_blank" rel="noreferrer" className="inline-flex min-h-11 items-center self-start text-base text-action underline">
            {texto(`copago.wompi.aceptacion.leer-${clave}`)}
          </a>
        </div>
      ))}
      {faltaAceptar && !(aceptacion.terminos && aceptacion.datos) && (
        <p role="alert" className="text-sm font-medium text-danger-fg">
          {texto("copago.wompi.aceptacion.falta")}
        </p>
      )}
    </div>
  );
}
