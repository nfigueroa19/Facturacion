import type { CheckoutPago } from "./tipos";

// API del sandbox documentada en https://docs.wompi.co/docs/colombia/ (consultada el 2026-10-24). El backend rechaza
// llaves de producción, así que el navegador solo habla con el sandbox.
const URL_WOMPI = "https://sandbox.wompi.co/v1";

/** Datos del medio de pago tal como los digita el pagador. Viajan del navegador a Wompi, nunca al backend. */
export type MedioWompi =
  | { readonly tipo: "CARD"; readonly numero: string; readonly titular: string; readonly mes: string; readonly anio: string; readonly cvc: string }
  | { readonly tipo: "NEQUI"; readonly celular: string }
  | { readonly tipo: "PSE"; readonly banco: string; readonly tipoDocumento: string; readonly documento: string };

export interface BancoPse {
  readonly codigo: string;
  readonly nombre: string;
}

/** Wompi respondió 422: rechazó un dato del medio de pago (tarjeta, celular, documento o correo). */
export class ErrorWompi extends Error {}

export interface PasarelaNavegador {
  bancosPse(llavePublica: string): Promise<readonly BancoPse[]>;
  /**
   * Tokeniza la tarjeta si hace falta, crea la transacción con la referencia y el monto del backend y espera a que Wompi
   * la finalice. Devuelve su ID aunque siga PENDING: el backend decide si el copago quedó pagado.
   */
  pagar(checkout: CheckoutPago, medio: MedioWompi, correo: string): Promise<string>;
}

const PSE_DESCRIPCION = "Copago consulta externa HSLV";

// Nequi y PSE quedan PENDING unos segundos en el sandbox; 15 consultas cada 2 s cubren de sobra lo observado.
const CONSULTAS = 15;
const ESPERA_MS = 2000;

type Pausa = (ms: number) => Promise<void>;

export function pasarelaWompi(pedir: typeof fetch = fetch, pausa: Pausa = (ms) => new Promise((r) => setTimeout(r, ms))): PasarelaNavegador {
  async function wompi<T>(ruta: string, llave: string | null, cuerpo?: unknown): Promise<T> {
    const cabeceras: Record<string, string> = cuerpo === undefined ? {} : { "Content-Type": "application/json" };
    if (llave !== null) cabeceras.Authorization = `Bearer ${llave}`;
    const respuesta = await pedir(`${URL_WOMPI}${ruta}`, { method: cuerpo === undefined ? "GET" : "POST", headers: cabeceras, body: JSON.stringify(cuerpo) });
    if (respuesta.status === 422) throw new ErrorWompi(`Wompi rechazó los datos en ${ruta}`);
    const json = (await respuesta.json().catch(() => null)) as { data?: T } | null;
    if (!respuesta.ok || json?.data === undefined) throw new Error(`Wompi respondió ${respuesta.status} en ${ruta}`);
    return json.data;
  }

  async function metodoDePago(llave: string, medio: MedioWompi) {
    switch (medio.tipo) {
      case "CARD": {
        const tarjeta = { number: medio.numero, card_holder: medio.titular, exp_month: medio.mes, exp_year: medio.anio, cvc: medio.cvc };
        const { id } = await wompi<{ id: string }>("/tokens/cards", llave, tarjeta);
        return { type: "CARD", token: id, installments: 1 };
      }
      case "NEQUI":
        return { type: "NEQUI", phone_number: medio.celular };
      case "PSE":
        return {
          type: "PSE",
          user_type: 0,
          user_legal_id_type: medio.tipoDocumento,
          user_legal_id: medio.documento,
          financial_institution_code: medio.banco,
          payment_description: PSE_DESCRIPCION,
        };
    }
  }

  return {
    async bancosPse(llave) {
      const bancos = await wompi<{ financial_institution_code: string; financial_institution_name: string }[]>("/pse/financial_institutions", llave);
      return bancos.map((banco) => ({ codigo: banco.financial_institution_code, nombre: banco.financial_institution_name }));
    },
    async pagar(checkout, medio, correo) {
      const transaccion = await wompi<{ id: string }>("/transactions", checkout.llavePublica, {
        acceptance_token: checkout.aceptacion.terminos.token,
        accept_personal_auth: checkout.aceptacion.datosPersonales.token,
        amount_in_cents: checkout.montoCentavos,
        currency: checkout.moneda,
        reference: checkout.referencia,
        customer_email: correo,
        payment_method: await metodoDePago(checkout.llavePublica, medio),
        ...(checkout.firma === null ? {} : { signature: checkout.firma }),
      });
      for (let consulta = 0; consulta < CONSULTAS; consulta++) {
        const { status } = await wompi<{ status: string }>(`/transactions/${encodeURIComponent(transaccion.id)}`, null);
        if (status !== "PENDING") break;
        await pausa(ESPERA_MS);
      }
      return transaccion.id;
    },
  };
}
