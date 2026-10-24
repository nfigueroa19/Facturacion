import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { App } from "./App";
import { clienteFacturadorHttp } from "./api/clienteFacturador";
import { clientePacienteHttp } from "./api/clientePaciente";
import { clientePersonalHttp } from "./api/clientePersonal";
import { clientePruebasHttp } from "./api/clientePruebas";
import { pasarelaWompi } from "./api/pasarelaWompi";
import { clienteContenidoHttp } from "./contenido/clienteContenido";
import { ProveedorContenido } from "./contenido/ContenidoContexto";
import "./estilos.css";

// La URL del backend se fija al compilar; en desarrollo apunta al Ktor local de `gradlew run`.
const URL_API = import.meta.env.VITE_URL_API ?? "http://127.0.0.1:8080";
const contenido = clienteContenidoHttp(URL_API);
const clientes = {
  paciente: clientePacienteHttp(URL_API),
  facturador: clienteFacturadorHttp(URL_API),
  personal: clientePersonalHttp(URL_API),
  pruebas: clientePruebasHttp(URL_API),
  pasarela: pasarelaWompi,
};

const raiz = document.getElementById("raiz");
if (raiz === null) throw new Error("Falta el elemento #raiz en index.html");

createRoot(raiz).render(
  <StrictMode>
    <ProveedorContenido cliente={contenido}>
      <App clientes={clientes} ruta={window.location.pathname} />
    </ProveedorContenido>
  </StrictMode>,
);
