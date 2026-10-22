import { useContenido } from "./contenido/ContenidoContexto";
import { EsqueletoAcceso } from "./componentes/EsqueletoAcceso";
import { ErrorContenido } from "./componentes/ErrorContenido";
import { Marco } from "./componentes/Marco";
import { PantallaAcceso } from "./pantallas/PantallaAcceso";

function PantallaSegunContenido() {
  const { estado, reintentar } = useContenido();
  switch (estado.tipo) {
    case "cargando":
      return <EsqueletoAcceso />;
    case "error":
      return <ErrorContenido onReintentar={reintentar} />;
    case "listo":
      return <PantallaAcceso textos={estado.textos} />;
  }
}

export function App() {
  return (
    <Marco>
      <PantallaSegunContenido />
    </Marco>
  );
}
