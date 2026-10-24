import { Button, Checkbox, FieldError, Form, Input, Label, Radio, RadioGroup, TextField } from "@heroui/react";
import { useState, type FormEvent } from "react";
import type { ErrorNuevaCita, NuevaCita, Regimen } from "../../api/tipos";
import type { Texto } from "../../contenido/ContenidoContexto";

interface Props {
  readonly texto: Texto;
  /** Errores que devolvió el backend; cada uno se muestra en su campo. */
  readonly errores: readonly ErrorNuevaCita[];
  readonly enviando: boolean;
  readonly onProgramar: (cita: NuevaCita) => void;
}

const CAMPOS = [
  { nombre: "identificacion", error: "IDENTIFICACION", tipo: "text", modo: "numeric" },
  { nombre: "celular", error: "CELULAR", tipo: "tel", modo: "tel" },
  { nombre: "fecha", error: "FECHA", tipo: "date", modo: undefined },
  { nombre: "medico", error: "MEDICO", tipo: "text", modo: undefined },
  { nombre: "especialidad", error: "ESPECIALIDAD", tipo: "text", modo: undefined },
] as const;

const hoy = () => new Date().toLocaleDateString("en-CA");

export function FormularioCita({ texto, errores, enviando, onProgramar }: Props) {
  const [regimen, setRegimen] = useState<Regimen>("CONTRIBUTIVO");
  const [conCopago, setConCopago] = useState(false);

  const enviar = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    const datos = new FormData(evento.currentTarget);
    const valor = (campo: string) => String(datos.get(campo) ?? "");
    onProgramar({
      identificacion: valor("identificacion"),
      celular: valor("celular"),
      fecha: valor("fecha"),
      medico: valor("medico"),
      especialidad: valor("especialidad"),
      regimen,
      copagoPesos: conCopago ? Number(valor("copago")) : null,
    });
  };

  return (
    <Form onSubmit={enviar} className="flex flex-col gap-4" validationBehavior="aria">
      <div className="grid gap-4 sm:grid-cols-2">
        {CAMPOS.map((campo) => (
          <TextField
            key={campo.nombre}
            name={campo.nombre}
            type={campo.tipo}
            inputMode={campo.modo}
            defaultValue={campo.nombre === "fecha" ? hoy() : undefined}
            isRequired
            isInvalid={errores.includes(campo.error)}
            className="flex flex-col gap-1.5"
          >
            <Label className="text-sm font-medium text-ink">{texto(`callcenter.campo.${campo.nombre}`)}</Label>
            <Input className="min-h-10 text-base" />
            <FieldError className="text-sm font-medium text-danger-fg">{texto(`callcenter.error.${campo.error}`)}</FieldError>
          </TextField>
        ))}
      </div>
      <RadioGroup value={regimen} onChange={(valor) => setRegimen(valor as Regimen)} orientation="horizontal" className="flex flex-col gap-2">
        <Label className="text-sm font-medium text-ink">{texto("cita.regimen")}</Label>
        <div className="flex flex-wrap gap-4">
          {(["CONTRIBUTIVO", "SUBSIDIADO"] as const).map((opcion) => (
            <Radio key={opcion} value={opcion}>
              <Radio.Content className="text-sm text-ink">
                <Radio.Control>
                  <Radio.Indicator />
                </Radio.Control>
                <Label>{texto(`regimen.${opcion}`)}</Label>
              </Radio.Content>
            </Radio>
          ))}
        </div>
      </RadioGroup>
      <Checkbox isSelected={conCopago} onChange={setConCopago}>
        <Checkbox.Content className="text-sm text-ink">
          <Checkbox.Control>
            <Checkbox.Indicator />
          </Checkbox.Control>
          <Label>{texto("callcenter.campo.activar-copago")}</Label>
        </Checkbox.Content>
      </Checkbox>
      {conCopago && (
        <TextField name="copago" type="number" inputMode="numeric" isRequired isInvalid={errores.includes("COPAGO")} className="flex max-w-xs flex-col gap-1.5">
          <Label className="text-sm font-medium text-ink">{texto("callcenter.campo.copago")}</Label>
          <Input min={1} className="min-h-10 text-base tabular-nums" />
          <FieldError className="text-sm font-medium text-danger-fg">{texto("callcenter.error.COPAGO")}</FieldError>
        </TextField>
      )}
      <Button type="submit" isPending={enviando} className="self-start">
        {texto("callcenter.programar")}
      </Button>
    </Form>
  );
}
