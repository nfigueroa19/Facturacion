import { useCallback, useEffect, useState } from "react";
import type { ClientePaciente } from "../../api/clientePaciente";
import { esSesionVencida } from "../../api/http";
import type { PasarelaNavegador } from "../../api/pasarelaWompi";
import type { CatalogoPruebas } from "../../api/tipos";
import { admiteCambiosDelPaciente } from "../../api/tipos";
import { DatosPrueba } from "../../componentes/DatosPrueba";
import { ErrorCarga } from "../../componentes/ErrorCarga";
import { EsqueletoAcceso } from "../../componentes/EsqueletoAcceso";
import type { Texto } from "../../contenido/ContenidoContexto";
import { useCarga } from "../../useCarga";
import { FlujoRadicacion } from "./FlujoRadicacion";
import { PantallaAcceso, type ResultadoIngreso } from "./PantallaAcceso";
import { PantallaEstado } from "./PantallaEstado";

/** El token vive solo en memoria: al recargar la página el paciente ingresa de nuevo. */
interface Props {
  readonly cliente: ClientePaciente;
  readonly pasarela: PasarelaNavegador;
  readonly texto: Texto;
  readonly catalogo: CatalogoPruebas | null;
}

export function AppPaciente({ cliente, pasarela, texto, catalogo }: Props) {
  const [token, setToken] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const ingresar = async (identificacion: string, codigo: string): Promise<ResultadoIngreso> => {
    try {
      const nuevo = await cliente.iniciarSesion(identificacion, codigo);
      if (nuevo === null) return "credenciales";
      setAviso(null);
      setToken(nuevo);
      return "ingreso";
    } catch {
      return "conexion";
    }
  };

  const salir = useCallback((motivo: string | null) => {
    setToken(null);
    setAviso(motivo);
  }, []);

  if (token === null) {
    return (
      <div className="flex flex-col gap-8">
        <PantallaAcceso texto={texto} onIngresar={ingresar} aviso={aviso} />
        {catalogo !== null && <DatosPrueba catalogo={catalogo} texto={texto} pestanaInicial="pacientes" />}
      </div>
    );
  }
  return <SesionPaciente cliente={cliente} pasarela={pasarela} token={token} texto={texto} onSalir={salir} />;
}

interface PropsSesion {
  readonly cliente: ClientePaciente;
  readonly pasarela: PasarelaNavegador;
  readonly token: string;
  readonly texto: Texto;
  readonly onSalir: (motivo: string | null) => void;
}

function SesionPaciente({ cliente, pasarela, token, texto, onSalir }: PropsSesion) {
  const { carga, recargar } = useCarga((senal) => cliente.prefactura(token, senal), [token]);
  const [corrigiendo, setCorrigiendo] = useState(false);
  const vencida = carga.tipo === "error" && esSesionVencida(carga.error);

  useEffect(() => {
    if (vencida) onSalir("acceso.sesion-vencida");
  }, [vencida, onSalir]);

  const releer = () => {
    setCorrigiendo(false);
    recargar();
  };

  if (carga.tipo === "cargando" || vencida) return <EsqueletoAcceso />;
  if (carga.tipo === "error") return <ErrorCarga texto={texto} titulo="paciente.error-carga" onReintentar={recargar} />;

  const prefactura = carga.valor;
  const editable = admiteCambiosDelPaciente(prefactura.solicitud);
  if (prefactura.solicitud !== null && !(editable && corrigiendo)) {
    return (
      <PantallaEstado
        prefactura={prefactura}
        solicitud={prefactura.solicitud}
        texto={texto}
        onCorregir={editable ? () => setCorrigiendo(true) : null}
        onSalir={() => onSalir(null)}
      />
    );
  }
  return (
    <FlujoRadicacion
      // Tras releer, el flujo arranca de nuevo con los datos que guardó el servidor.
      key={prefactura.solicitud?.fechaEnvio ?? "nueva"}
      cliente={cliente}
      pasarela={pasarela}
      token={token}
      prefactura={prefactura}
      texto={texto}
      onRadicada={releer}
      onSesionVencida={() => onSalir("acceso.sesion-vencida")}
    />
  );
}
