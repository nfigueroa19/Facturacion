import type { CheckoutPago } from "./tipos";

/** Checkout de la pasarela en el navegador. La tarjeta se digita dentro del widget y nunca pasa por esta aplicación. */
export interface PasarelaNavegador {
  /**
   * Resuelve cuando el widget está abierto. `alTerminar` recibe el ID de la transacción cuando Wompi la finaliza;
   * no se llama si el paciente cierra el widget sin pagar.
   */
  abrir(checkout: CheckoutPago, alTerminar: (transaccion: string) => void): Promise<void>;
}

// Documentado en https://docs.wompi.co/docs/colombia/widget-checkout-web/ (consultado el 2026-10-24).
const URL_WIDGET = "https://checkout.wompi.co/widget.js";

interface OpcionesWidget {
  readonly currency: string;
  readonly amountInCents: number;
  readonly reference: string;
  readonly publicKey: string;
  readonly signature: { readonly integrity: string };
}

interface WidgetCheckout {
  open(alCerrar: (resultado: { readonly transaction?: { readonly id: string } }) => void): void;
}

declare global {
  interface Window {
    WidgetCheckout?: new (opciones: OpcionesWidget) => WidgetCheckout;
  }
}

// El script se carga la primera vez que el paciente paga, no al abrir la web; si falla, el siguiente intento lo repite.
let carga: Promise<void> | null = null;

function cargarWidget(): Promise<void> {
  carga ??= new Promise<void>((resolver, rechazar) => {
    const script = document.createElement("script");
    script.src = URL_WIDGET;
    script.async = true;
    script.onload = () => resolver();
    script.onerror = () => {
      carga = null;
      script.remove();
      rechazar(new Error("No cargó el widget de Wompi"));
    };
    document.head.append(script);
  });
  return carga;
}

export const pasarelaWompi: PasarelaNavegador = {
  async abrir(checkout, alTerminar) {
    await cargarWidget();
    const Widget = window.WidgetCheckout;
    if (Widget === undefined) throw new Error("El script de Wompi no definió WidgetCheckout");
    const widget = new Widget({
      currency: checkout.moneda,
      amountInCents: checkout.montoCentavos,
      reference: checkout.referencia,
      publicKey: checkout.llavePublica,
      signature: { integrity: checkout.firma },
    });
    widget.open((resultado) => {
      if (resultado.transaction !== undefined) alTerminar(resultado.transaction.id);
    });
  },
};
