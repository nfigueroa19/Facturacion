import { Alert, Button } from "@heroui/react";
import type { Texto } from "../contenido/ContenidoContexto";

/** Fallo al pedir datos con los textos ya cargados: el mensaje sale de contenido.json. */
export function ErrorCarga({ texto, titulo, onReintentar }: { texto: Texto; titulo: string; onReintentar: () => void }) {
  return (
    <div className="aparicion-estado flex flex-col items-start gap-4">
      <Alert status="danger">
        <Alert.Indicator />
        <Alert.Content>
          <Alert.Title>{texto(titulo)}</Alert.Title>
          <Alert.Description>{texto("general.revise-conexion")}</Alert.Description>
        </Alert.Content>
      </Alert>
      <Button variant="outline" onPress={onReintentar}>
        {texto("general.reintentar")}
      </Button>
    </div>
  );
}
