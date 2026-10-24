import { FieldError, Input, Label, ListBox, Select, Skeleton, TextField } from "@heroui/react";
import type { BancoPse } from "../../api/pasarelaWompi";
import { ErrorCarga } from "../../componentes/ErrorCarga";
import type { Texto } from "../../contenido/ContenidoContexto";
import { useCarga } from "../../useCarga";

interface PropsCampo {
  readonly nombre: string;
  readonly texto: Texto;
  readonly valido: (valor: string) => boolean;
  readonly autoComplete: string;
  readonly modo?: "numeric" | "tel" | "email";
  readonly tipo?: "text" | "email" | "tel";
  readonly largo?: number;
}

/** Etiqueta y error salen de `copago.wompi.campo.<nombre>`; el error se asocia al campo y lo anuncia el lector de pantalla. */
export function CampoWompi({ nombre, texto, valido, autoComplete, modo, tipo = "text", largo }: PropsCampo) {
  const error = texto(`copago.wompi.campo.${nombre}.error`);
  return (
    <TextField
      name={nombre}
      type={tipo}
      inputMode={modo}
      isRequired
      autoComplete={autoComplete}
      validate={(valor) => (valido(valor.trim()) ? null : error)}
      className="flex flex-col gap-2"
    >
      <Label className="text-base font-medium text-ink">{texto(`copago.wompi.campo.${nombre}`)}</Label>
      <Input maxLength={largo} className="min-h-11 text-base tabular-nums" />
      <FieldError className="text-sm font-medium text-danger-fg">{error}</FieldError>
    </TextField>
  );
}

export const sinEspacios = (valor: string) => valor.replace(/\s/g, "");

// Wompi rechaza con 422 un número que no pasa Luhn; se valida aquí para señalar el campo exacto.
function pasaLuhn(numero: string) {
  const suma = [...numero].reverse().reduce((total, caracter, posicion) => {
    const digito = Number(caracter) * (posicion % 2 === 1 ? 2 : 1);
    return total + (digito > 9 ? digito - 9 : digito);
  }, 0);
  return suma % 10 === 0;
}

export function CamposTarjeta({ texto }: { texto: Texto }) {
  return (
    <div className="flex flex-col gap-4">
      <CampoWompi
        nombre="numero"
        texto={texto}
        modo="numeric"
        autoComplete="cc-number"
        largo={23}
        valido={(valor) => /^\d{13,19}$/.test(sinEspacios(valor)) && pasaLuhn(sinEspacios(valor))}
      />
      {/* Wompi exige al menos 5 caracteres en el nombre del titular. */}
      <CampoWompi nombre="titular" texto={texto} autoComplete="cc-name" largo={60} valido={(valor) => valor.length >= 5} />
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
        <CampoWompi nombre="mes" texto={texto} modo="numeric" autoComplete="cc-exp-month" largo={2} valido={(valor) => /^(0?[1-9]|1[0-2])$/.test(valor)} />
        <CampoWompi nombre="anio" texto={texto} modo="numeric" autoComplete="cc-exp-year" largo={2} valido={(valor) => /^\d{2}$/.test(valor)} />
        <CampoWompi nombre="cvc" texto={texto} modo="numeric" autoComplete="cc-csc" largo={4} valido={(valor) => /^\d{3,4}$/.test(valor)} />
      </div>
    </div>
  );
}

export function CamposNequi({ texto }: { texto: Texto }) {
  return <CampoWompi nombre="celular" texto={texto} tipo="tel" modo="tel" autoComplete="tel-national" largo={10} valido={(valor) => /^3\d{9}$/.test(valor)} />;
}

// Documentos de persona natural: la transacción PSE va con user_type 0.
const TIPOS_DOCUMENTO = ["CC", "CE", "TI", "PP"] as const;

interface PropsPse {
  readonly texto: Texto;
  readonly bancos: (senal: AbortSignal) => Promise<readonly BancoPse[]>;
}

export function CamposPse({ texto, bancos }: PropsPse) {
  const { carga, recargar } = useCarga(bancos, []);
  if (carga.tipo === "cargando") {
    return (
      <div aria-busy="true" aria-label={texto("copago.wompi.bancos.cargando")} className="flex flex-col gap-2">
        <Skeleton className="h-5 w-32 rounded-[6px]" />
        <Skeleton className="h-11 w-full rounded-[6px]" />
      </div>
    );
  }
  if (carga.tipo === "error") return <ErrorCarga texto={texto} titulo="copago.wompi.bancos.error" onReintentar={recargar} />;
  return (
    <div className="flex flex-col gap-4">
      <Lista nombre="banco" texto={texto} opciones={carga.valor.map((banco) => ({ id: banco.codigo, nombre: banco.nombre }))} />
      <Lista nombre="tipoDocumento" texto={texto} opciones={TIPOS_DOCUMENTO.map((tipo) => ({ id: tipo, nombre: texto(`copago.wompi.documento.${tipo}`) }))} />
      <CampoWompi nombre="documento" texto={texto} modo="numeric" autoComplete="off" largo={15} valido={(valor) => /^[0-9A-Za-z]{5,15}$/.test(valor)} />
    </div>
  );
}

function Lista({ nombre, texto, opciones }: { nombre: string; texto: Texto; opciones: readonly { id: string; nombre: string }[] }) {
  return (
    <Select name={nombre} isRequired placeholder={texto("copago.wompi.elegir")} className="flex flex-col gap-2">
      <Label className="text-base font-medium text-ink">{texto(`copago.wompi.campo.${nombre}`)}</Label>
      <Select.Trigger className="min-h-11 text-base">
        <Select.Value />
        <Select.Indicator />
      </Select.Trigger>
      <Select.Popover>
        <ListBox>
          {opciones.map((opcion) => (
            <ListBox.Item key={opcion.id} id={opcion.id} textValue={opcion.nombre} className="min-h-11 text-base">
              {opcion.nombre}
              <ListBox.ItemIndicator />
            </ListBox.Item>
          ))}
        </ListBox>
      </Select.Popover>
      <FieldError className="text-sm font-medium text-danger-fg">{texto(`copago.wompi.campo.${nombre}.error`)}</FieldError>
    </Select>
  );
}
