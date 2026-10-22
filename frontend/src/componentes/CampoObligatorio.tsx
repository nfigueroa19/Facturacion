import { Description, FieldError, Input, Label, TextField } from "@heroui/react";

interface Props {
  readonly nombre: string;
  readonly etiqueta: string;
  readonly mensajeVacio: string;
  readonly ayuda?: string;
}

/** Campo de texto con etiqueta visible; el error se asocia al campo y lo anuncia el lector de pantalla. */
export function CampoObligatorio({ nombre, etiqueta, mensajeVacio, ayuda }: Props) {
  return (
    <TextField
      name={nombre}
      isRequired
      autoComplete="off"
      validate={(valor) => (valor.trim() === "" ? mensajeVacio : null)}
      className="flex flex-col gap-2"
    >
      <Label className="text-base font-medium text-ink">{etiqueta}</Label>
      <Input className="min-h-11 text-base sm:text-base" />
      {ayuda && <Description className="text-sm text-ink-subtle">{ayuda}</Description>}
      {/* La única regla del campo es no estar vacío; así el mensaje sale de contenido.json y no del navegador. */}
      <FieldError className="text-sm font-medium text-danger-fg">{mensajeVacio}</FieldError>
    </TextField>
  );
}
