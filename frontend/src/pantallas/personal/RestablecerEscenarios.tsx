import { Alert, Button, Modal } from "@heroui/react";
import { useState } from "react";
import type { ClientePruebas } from "../../api/clientePruebas";
import { esSesionVencida } from "../../api/http";
import type { Texto } from "../../contenido/ContenidoContexto";
import { conValores } from "../../formato";

type Estado = { readonly tipo: "confirmar" } | { readonly tipo: "enviando" } | { readonly tipo: "listo"; readonly escenarios: number } | { readonly tipo: "error" };

interface Props {
  readonly cliente: ClientePruebas;
  readonly token: string;
  readonly texto: Texto;
  readonly onSesionVencida: () => void;
}

/** Borra lo que hicieron los probadores con las identificaciones del catálogo. Afecta a todos, por eso pide confirmación. */
export function RestablecerEscenarios({ cliente, token, texto, onSesionVencida }: Props) {
  const [estado, setEstado] = useState<Estado>({ tipo: "confirmar" });

  const restablecer = async () => {
    setEstado({ tipo: "enviando" });
    try {
      setEstado({ tipo: "listo", escenarios: await cliente.restablecer(token) });
    } catch (error) {
      if (esSesionVencida(error)) return onSesionVencida();
      setEstado({ tipo: "error" });
    }
  };

  return (
    <Modal
      onOpenChange={(abierto) => {
        if (!abierto) setEstado({ tipo: "confirmar" });
      }}
    >
      <Button variant="outline" size="sm">
        {texto("pruebas.restablecer.abrir")}
      </Button>
      <Modal.Backdrop>
        <Modal.Container size="sm">
          <Modal.Dialog className="rounded-[6px]">
            <Modal.Header>
              <Modal.Heading className="text-lg font-bold text-brand-navy">{texto("pruebas.restablecer.titulo")}</Modal.Heading>
            </Modal.Header>
            <Modal.Body className="flex flex-col gap-3">
              <p className="text-sm text-ink">{texto("pruebas.restablecer.explicacion")}</p>
              {estado.tipo === "listo" && (
                <Alert status="success" role="status">
                  <Alert.Indicator />
                  <Alert.Content>
                    <Alert.Description>{conValores(texto("pruebas.restablecer.listo"), { escenarios: estado.escenarios })}</Alert.Description>
                  </Alert.Content>
                </Alert>
              )}
              {estado.tipo === "error" && (
                <Alert status="danger" role="alert">
                  <Alert.Indicator />
                  <Alert.Content>
                    <Alert.Description>{texto("pruebas.restablecer.error")}</Alert.Description>
                  </Alert.Content>
                </Alert>
              )}
            </Modal.Body>
            <Modal.Footer>
              <Button slot="close" variant="outline">
                {texto(estado.tipo === "listo" ? "pruebas.cerrar" : "pruebas.restablecer.cancelar")}
              </Button>
              {estado.tipo !== "listo" && (
                <Button variant="danger" isPending={estado.tipo === "enviando"} onPress={restablecer}>
                  {texto("pruebas.restablecer.confirmar")}
                </Button>
              )}
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
