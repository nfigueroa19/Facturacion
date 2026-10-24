import { render } from "@testing-library/react";
import { vi } from "vitest";
import { App, type Clientes } from "../App";
import type { ClienteFacturador } from "../api/clienteFacturador";
import type { ClientePaciente } from "../api/clientePaciente";
import type { ClientePersonal } from "../api/clientePersonal";
import type { DetalleSolicitud, PrefacturaPaciente, ResumenSolicitud, Turno } from "../api/tipos";
import type { ClienteContenido, Textos } from "../contenido/clienteContenido";
import { ProveedorContenido } from "../contenido/ContenidoContexto";

// Datos ficticios. Los textos de prueba son las claves mismas, salvo los que la prueba necesita leer.
export const TEXTOS: Textos = {
  "prefactura.titulo": "Prefactura de prueba",
  "acceso.campo-identificacion": "Identificación de prueba",
  "acceso.campo-id-acceso": "ID de prueba",
  "acceso.dato-obligatorio": "Dato obligatorio de prueba",
  "acceso.boton": "Ingresar",
  "acceso.error-credenciales": "Credenciales erradas de prueba",
  "pasos.continuar": "Continuar",
  "envio.boton": "Enviar solicitud",
  "copago.pagar": "Pagar {valor}",
  "copago.sin-pagar": "Enviar sin pagar",
  "datos.error.FALTA": "Falta el dato",
  "soportes.AUTORIZACION": "Autorización",
  "soportes.ORDEN_MEDICA": "Orden médica",
  "soportes.error.NO_ES_PDF": "No es PDF",
  "soportes.faltan": "Faltan soportes",
  "estado.corregir": "Corregir la solicitud",
  "facturador.acceso.usuario": "Usuario",
  "facturador.acceso.clave": "Clave",
  "facturador.acceso.boton": "Ingresar",
  "acciones.aprobar": "Aprobar solicitud",
  "acciones.checklist-incompleto": "Checklist incompleto",
  "checklist.DATOS": "Datos revisados",
  "checklist.AUTORIZACION": "Autorización revisada",
  "checklist.ORDEN_MEDICA": "Orden revisada",
};

export const CITA = { fecha: "2026-10-23", medico: "Médico de prueba", especialidad: "Especialidad", regimen: "CONTRIBUTIVO", copagoPesos: 5000 } as const;

export const PREFACTURA: PrefacturaPaciente = {
  cita: CITA,
  formulario: [{ clave: "nombre-padre", etiqueta: "Nombre del padre", tipo: "TEXTO", obligatorio: true, ayuda: null }],
  soportes: [],
  pago: null,
  solicitud: null,
  tamanoMaximoSoporteBytes: 1024,
};

export const RESUMEN: ResumenSolicitud = {
  consecutivo: "PF-000001",
  estado: "EN_REVISION",
  fechaEnvio: "2026-10-22T13:00:00Z",
  identificacion: "1000000001",
  cita: { ...CITA, copagoPesos: null },
  soportes: ["AUTORIZACION", "ORDEN_MEDICA"],
  pago: null,
};

export const DETALLE: DetalleSolicitud = {
  resumen: RESUMEN,
  datos: [{ clave: "nombre-padre", etiqueta: "Nombre del padre", valor: "Padre de prueba" }],
  novedades: [],
  validacion: null,
  checklist: ["DATOS", "AUTORIZACION", "ORDEN_MEDICA"],
  atencion: { ingreso: null, intentosFallidos: [], factura: null, anulacion: null },
};

export function clientePaciente(cambios: Partial<ClientePaciente> = {}): ClientePaciente {
  return {
    iniciarSesion: vi.fn(() => Promise.resolve("token")),
    prefactura: vi.fn(() => Promise.resolve(PREFACTURA)),
    subirSoporte: vi.fn(() => Promise.resolve({ tipo: "cargado" as const })),
    pagarCopago: vi.fn(() => Promise.resolve({ tipo: "pagado" as const, pago: { referencia: "SIM-1", valorPesos: 5000, fecha: "2026-10-22T13:00:00Z" } })),
    radicar: vi.fn(() => Promise.resolve({ tipo: "radicada" as const, consecutivo: "PF-000001", estado: "EN_REVISION" as const })),
    ...cambios,
  };
}

export function clienteFacturador(cambios: Partial<ClienteFacturador> = {}): ClienteFacturador {
  return {
    iniciarSesion: vi.fn(() => Promise.resolve({ token: "token", nombre: "Facturador de prueba" })),
    solicitudes: vi.fn(() => Promise.resolve([RESUMEN])),
    detalle: vi.fn(() => Promise.resolve(DETALLE)),
    soporte: vi.fn(() => Promise.resolve(new Blob(["%PDF-1.7"]))),
    aprobar: vi.fn(() => Promise.resolve()),
    registrarNovedad: vi.fn(() => Promise.resolve()),
    confirmarLlegada: vi.fn(() => Promise.resolve()),
    registrarIngreso: vi.fn(() => Promise.resolve()),
    anularIngreso: vi.fn(() => Promise.resolve()),
    registrarIntentoFallido: vi.fn(() => Promise.resolve()),
    registrarFactura: vi.fn(() => Promise.resolve()),
    ...cambios,
  };
}

export const TURNO_PRECONSULTA: Turno = {
  turno: "P-001",
  destino: "PRECONSULTA",
  motivo: null,
  cita: CITA,
  consecutivo: "PF-000001",
  estado: "LISTO_PARA_FACTURAR",
  novedades: [],
};

export function clientePersonal(cambios: Partial<ClientePersonal> = {}): ClientePersonal {
  return {
    programarCita: vi.fn(() =>
      Promise.resolve({ tipo: "programada" as const, programada: { identificacion: "1000000099", cita: CITA, codigoAcceso: "ABCD2345", sms: null } }),
    ),
    atenderLlegada: vi.fn(() => Promise.resolve(TURNO_PRECONSULTA)),
    comunicaciones: vi.fn(() => Promise.resolve([])),
    ...cambios,
  };
}

export function contenido(obtenerTextos: ClienteContenido["obtenerTextos"] = () => Promise.resolve(TEXTOS)): ClienteContenido {
  return { obtenerTextos, urlImagen: (clave) => `/imagenes/${clave}` };
}

export function montar(opciones: { contenido?: ClienteContenido; clientes?: Partial<Clientes>; ruta?: string } = {}) {
  const clientes: Clientes = { paciente: clientePaciente(), facturador: clienteFacturador(), personal: clientePersonal(), ...opciones.clientes };
  return render(
    <ProveedorContenido cliente={opciones.contenido ?? contenido()}>
      <App clientes={clientes} ruta={opciones.ruta ?? "/"} />
    </ProveedorContenido>,
  );
}

export const pdf = (contenido = "%PDF-1.7 prueba", nombre = "soporte.pdf") => new File([contenido], nombre, { type: "application/pdf" });
