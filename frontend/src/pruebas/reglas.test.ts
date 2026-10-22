import { describe, expect, it } from "vitest";
import type { Campo } from "../api/tipos";
import { conValores } from "../formato";
import { filtrar } from "../pantallas/facturador/filtros";
import { errorDeCampo, motivoRechazoPdf } from "../pantallas/paciente/validacion";
import { RESUMEN } from "./dobles";

const campo = (tipo: Campo["tipo"], obligatorio = true): Campo => ({ clave: "c", etiqueta: "C", tipo, obligatorio, ayuda: null });

describe("validación local", () => {
  it("replica las reglas técnicas del backend por tipo de campo", () => {
    expect(errorDeCampo(campo("TEXTO"), "  ")).toBe("FALTA");
    expect(errorDeCampo(campo("CORREO", false), "")).toBeNull();
    expect(errorDeCampo(campo("CORREO"), "sin-arroba")).toBe("FORMATO");
    expect(errorDeCampo(campo("TELEFONO"), "+57 300 123 4567")).toBeNull();
    expect(errorDeCampo(campo("TELEFONO"), "300-123")).toBe("FORMATO");
    expect(errorDeCampo(campo("TEXTO"), "x".repeat(121))).toBe("LARGO");
  });

  it("revisa el PDF por su firma y su tamaño, no por la extensión", async () => {
    expect(await motivoRechazoPdf(new Blob([]), 10)).toBe("VACIO");
    expect(await motivoRechazoPdf(new Blob(["%PDF-1.7 grande"]), 10)).toBe("EXCEDE_TAMANO");
    expect(await motivoRechazoPdf(new Blob(["\u0089PNG"]), 10)).toBe("NO_ES_PDF");
    expect(await motivoRechazoPdf(new Blob(["%PDF-1.7"]), 10)).toBeNull();
  });
});

describe("filtros del panel", () => {
  it("busca por identificación o radicado y por fecha de la cita", () => {
    const filtros = { estado: null, texto: "", fechaCita: "" };
    expect(filtrar([RESUMEN], { ...filtros, texto: "pf-0000" })).toHaveLength(1);
    expect(filtrar([RESUMEN], { ...filtros, texto: "100000" })).toHaveLength(1);
    expect(filtrar([RESUMEN], { ...filtros, fechaCita: "2026-10-24" })).toHaveLength(0);
  });

  it("reemplaza los valores de un texto y deja visibles las marcas sin valor", () => {
    expect(conValores("Paso {actual} de {total}", { actual: 1 })).toBe("Paso 1 de {total}");
  });
});
