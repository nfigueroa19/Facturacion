import { useState } from "react";
import { useContenido } from "../contenido/ContenidoContexto";

interface Props {
  readonly clave: string;
  readonly alternativo: string;
  readonly className: string;
}

/** Si el backend no entrega la imagen (catálogo editado, archivo rechazado), se muestra el nombre en texto. */
export function LogoInstitucional({ clave, alternativo, className }: Props) {
  const { urlImagen } = useContenido();
  const [fallo, setFallo] = useState(false);
  if (fallo) return <span className="text-base font-bold">{alternativo}</span>;
  return <img src={urlImagen(clave)} alt={alternativo} className={className} onError={() => setFallo(true)} />;
}
