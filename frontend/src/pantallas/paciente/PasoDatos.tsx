import { Button, Description, FieldError, Form, Input, Label, TextField } from "@heroui/react";
import type { FormEvent } from "react";
import type { Campo, ErrorCampo } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";
import { errorDeCampo } from "./validacion";

const TIPO_INPUT: Readonly<Record<Campo["tipo"], { type: string; autoComplete: string; inputMode?: "tel" | "email" }>> = {
  TEXTO: { type: "text", autoComplete: "off" },
  FECHA: { type: "date", autoComplete: "off" },
  CORREO: { type: "email", autoComplete: "email", inputMode: "email" },
  TELEFONO: { type: "tel", autoComplete: "tel", inputMode: "tel" },
};

export function mensajeError(error: ErrorCampo, campo: Campo, texto: Texto): string {
  return texto(error === "FORMATO" ? `datos.error.FORMATO.${campo.tipo}` : `datos.error.${error}`);
}

interface Props {
  readonly campos: readonly Campo[];
  readonly valores: Readonly<Record<string, string>>;
  readonly erroresServidor: Readonly<Record<string, ErrorCampo>>;
  readonly texto: Texto;
  readonly onCambiar: (clave: string, valor: string) => void;
  readonly onContinuar: () => void;
}

export function PasoDatos({ campos, valores, erroresServidor, texto, onCambiar, onContinuar }: Props) {
  const errores = Object.fromEntries(
    campos.flatMap((campo) => {
      const error = erroresServidor[campo.clave];
      return error === undefined ? [] : [[campo.clave, mensajeError(error, campo, texto)]];
    }),
  );
  const enviar = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    onContinuar();
  };
  return (
    <Form onSubmit={enviar} validationErrors={errores} className="flex flex-col gap-5">
      <p className="text-base text-ink-muted">{texto("datos.instruccion")}</p>
      {campos.map((campo) => (
        <TextField
          key={campo.clave}
          name={campo.clave}
          type={TIPO_INPUT[campo.tipo].type}
          value={valores[campo.clave] ?? ""}
          onChange={(valor) => onCambiar(campo.clave, valor)}
          validate={(valor) => {
            const error = errorDeCampo(campo, valor);
            return error === null ? null : mensajeError(error, campo, texto);
          }}
          className="flex flex-col gap-2"
        >
          <Label className="text-base font-medium text-ink">
            {campo.etiqueta}
            {!campo.obligatorio && <span className="font-normal text-ink-subtle"> ({texto("datos.opcional")})</span>}
          </Label>
          <Input
            aria-required={campo.obligatorio}
            autoComplete={TIPO_INPUT[campo.tipo].autoComplete}
            inputMode={TIPO_INPUT[campo.tipo].inputMode}
            className="min-h-11 text-base sm:text-base"
          />
          {campo.ayuda !== null && <Description className="text-sm text-ink-subtle">{campo.ayuda}</Description>}
          <FieldError className="text-sm font-medium text-danger-fg" />
        </TextField>
      ))}
      <Button type="submit" size="lg" className="w-full sm:w-auto sm:self-start">
        {texto("pasos.continuar")}
      </Button>
    </Form>
  );
}
