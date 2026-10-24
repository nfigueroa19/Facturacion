import { describe, expect, it, vi } from "vitest";
import { ErrorWompi, pasarelaWompi } from "../api/pasarelaWompi";
import { CHECKOUT } from "./dobles";

type Respuesta = { readonly estado?: number; readonly cuerpo: unknown };

/** Imitación de la API del sandbox: cada ruta entrega sus respuestas en orden y se guarda cada petición. */
function wompiFalso(rutas: Record<string, Respuesta[]>) {
  const peticiones: { ruta: string; init: RequestInit | undefined }[] = [];
  const pedir = vi.fn((url: string | URL | Request, init?: RequestInit) => {
    const ruta = String(url).replace("https://sandbox.wompi.co/v1", "");
    peticiones.push({ ruta, init });
    const { estado = 200, cuerpo } = rutas[ruta]?.shift() ?? { estado: 404, cuerpo: {} };
    return Promise.resolve(new Response(JSON.stringify(cuerpo), { status: estado }));
  });
  const cuerpo = (ruta: string) => JSON.parse(String(peticiones.find((p) => p.ruta === ruta)?.init?.body)) as Record<string, unknown>;
  return { pedir: pedir as unknown as typeof fetch, peticiones, cuerpo };
}

const pausa = vi.fn(() => Promise.resolve());
const tarjeta = { tipo: "CARD", numero: "4242424242424242", titular: "Paciente Prueba", mes: "05", anio: "29", cvc: "123" } as const;

describe("Cliente del sandbox de Wompi", () => {
  it("tokeniza la tarjeta, crea la transacción con la referencia del backend y espera a que salga de PENDING", async () => {
    const wompi = wompiFalso({
      "/tokens/cards": [{ cuerpo: { data: { id: "tok_test_1" } } }],
      "/transactions": [{ estado: 201, cuerpo: { data: { id: "tx-1", status: "PENDING" } } }],
      "/transactions/tx-1": [{ cuerpo: { data: { status: "PENDING" } } }, { cuerpo: { data: { status: "APPROVED" } } }],
    });
    expect(await pasarelaWompi(wompi.pedir, pausa).pagar(CHECKOUT, tarjeta, "paciente@example.com")).toBe("tx-1");
    expect(wompi.cuerpo("/tokens/cards")).toEqual({ number: "4242424242424242", card_holder: "Paciente Prueba", exp_month: "05", exp_year: "29", cvc: "123" });
    expect(wompi.cuerpo("/transactions")).toEqual({
      acceptance_token: "tok-terminos",
      accept_personal_auth: "tok-datos",
      amount_in_cents: 500_000,
      currency: "COP",
      reference: "cita-1_0a0a",
      customer_email: "paciente@example.com",
      payment_method: { type: "CARD", token: "tok_test_1", installments: 1 },
    });
    expect(new Headers(wompi.peticiones[0]?.init?.headers).get("Authorization")).toBe("Bearer pub_test_prueba");
    expect(wompi.peticiones.map((p) => p.ruta)).toEqual(["/tokens/cards", "/transactions", "/transactions/tx-1", "/transactions/tx-1"]);
    expect(pausa).toHaveBeenCalledTimes(1);
  });

  it("envía la firma de integridad si el backend la calculó y arma Nequi y PSE sin tokenizar", async () => {
    const wompi = wompiFalso({
      "/transactions": [{ cuerpo: { data: { id: "tx-2" } } }, { cuerpo: { data: { id: "tx-3" } } }],
      "/transactions/tx-2": [{ cuerpo: { data: { status: "DECLINED" } } }],
      "/transactions/tx-3": [{ cuerpo: { data: { status: "APPROVED" } } }],
    });
    const cliente = pasarelaWompi(wompi.pedir, pausa);
    await cliente.pagar({ ...CHECKOUT, firma: "f".repeat(64) }, { tipo: "NEQUI", celular: "3992222222" }, "paciente@example.com");
    expect(wompi.cuerpo("/transactions")).toMatchObject({ signature: "f".repeat(64), payment_method: { type: "NEQUI", phone_number: "3992222222" } });
    await cliente.pagar(CHECKOUT, { tipo: "PSE", banco: "1", tipoDocumento: "CC", documento: "1999888777" }, "paciente@example.com");
    const pse = JSON.parse(String(wompi.peticiones.filter((p) => p.ruta === "/transactions")[1]?.init?.body)) as Record<string, unknown>;
    expect(pse.payment_method).toMatchObject({ type: "PSE", user_type: 0, user_legal_id_type: "CC", user_legal_id: "1999888777", financial_institution_code: "1" });
    expect(pse).not.toHaveProperty("signature");
  });

  it("un 422 de Wompi es un dato rechazado; otro error es una falla de la pasarela", async () => {
    const rechaza = wompiFalso({ "/tokens/cards": [{ estado: 422, cuerpo: { error: { type: "INPUT_VALIDATION_ERROR" } } }] });
    await expect(pasarelaWompi(rechaza.pedir, pausa).pagar(CHECKOUT, tarjeta, "paciente@example.com")).rejects.toBeInstanceOf(ErrorWompi);
    const falla = wompiFalso({ "/transactions": [{ estado: 502, cuerpo: {} }] });
    const pago = pasarelaWompi(falla.pedir, pausa).pagar(CHECKOUT, { tipo: "NEQUI", celular: "3991111111" }, "paciente@example.com");
    await expect(pago).rejects.not.toBeInstanceOf(ErrorWompi);
  });

  it("lista los bancos de PSE con la llave pública", async () => {
    const wompi = wompiFalso({ "/pse/financial_institutions": [{ cuerpo: { data: [{ financial_institution_code: "1", financial_institution_name: "Banco que aprueba" }] } }] });
    expect(await pasarelaWompi(wompi.pedir, pausa).bancosPse("pub_test_prueba")).toEqual([{ codigo: "1", nombre: "Banco que aprueba" }]);
  });
});
