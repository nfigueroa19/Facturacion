import { Alert, Button } from "@heroui/react";

/** Texto fijo en el código: si el backend no responde, no hay de dónde leer contenido.json. */
export function ErrorContenido({ onReintentar }: { onReintentar: () => void }) {
  return (
    <div className="aparicion-estado flex flex-col items-start gap-4">
      <Alert status="danger">
        <Alert.Indicator />
        <Alert.Content>
          <Alert.Title>No se pudo cargar la prefactura</Alert.Title>
          <Alert.Description>
            Revise su conexión e intente de nuevo. Si el problema continúa, el día de su cita acérquese a la ventanilla
            de facturación.
          </Alert.Description>
        </Alert.Content>
      </Alert>
      <Button variant="outline" size="lg" onPress={onReintentar}>
        Intentar de nuevo
      </Button>
    </div>
  );
}
