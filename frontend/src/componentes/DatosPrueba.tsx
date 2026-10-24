import { Button, Modal, Tabs } from "@heroui/react";
import type { ReactNode } from "react";
import type { CatalogoPruebas } from "../api/tipos";
import type { Texto } from "../contenido/ContenidoContexto";
import { conValores } from "../formato";

export type PestanaPruebas = "pacientes" | "personal" | "callcenter";

interface Props {
  readonly catalogo: CatalogoPruebas;
  readonly texto: Texto;
  /** La pestaña que interesa en la pantalla donde se abre: pacientes en la web del paciente, personal en los módulos. */
  readonly pestanaInicial: PestanaPruebas;
}

/**
 * Credenciales y casos del catálogo ficticio del piloto, para probar cada rol sin consultar archivos. Va debajo del
 * formulario de acceso y solo existe mientras el backend publique el catálogo.
 */
export function DatosPrueba({ catalogo, texto, pestanaInicial }: Props) {
  return (
    <section aria-labelledby="titulo-datos-prueba" className="flex flex-col items-start gap-2 border-t border-border pt-5">
      <h2 id="titulo-datos-prueba" className="text-base font-bold text-ink">
        {texto("pruebas.seccion")}
      </h2>
      <p className="text-sm text-ink-muted">{texto("pruebas.seccion-ayuda")}</p>
      <Modal>
        <Button variant="outline">{texto("pruebas.abrir")}</Button>
        <Modal.Backdrop>
          <Modal.Container size="lg" scroll="inside">
            <Modal.Dialog className="rounded-[6px]">
              <Modal.CloseTrigger aria-label={texto("pruebas.cerrar")} />
              <Modal.Header>
                <Modal.Heading className="text-lg font-bold text-brand-navy">{texto("pruebas.titulo")}</Modal.Heading>
                <p className="text-sm text-ink-muted">{texto("pruebas.ayuda")}</p>
              </Modal.Header>
              <Modal.Body>
                <Tabs variant="secondary" defaultSelectedKey={pestanaInicial}>
                  <Tabs.ListContainer>
                    <Tabs.List aria-label={texto("pruebas.pestanas")}>
                      {(["pacientes", "personal", "callcenter"] as const).map((pestana) => (
                        <Tabs.Tab key={pestana} id={pestana}>
                          {texto(`pruebas.pestana.${pestana}`)}
                          <Tabs.Indicator />
                        </Tabs.Tab>
                      ))}
                    </Tabs.List>
                  </Tabs.ListContainer>
                  <Tabs.Panel id="pacientes">
                    <Lista vacia={catalogo.pacientes.length === 0} texto={texto}>
                      {catalogo.pacientes.map((paciente) => (
                        <Caso key={paciente.identificacion} titulo={paciente.escenario} pasos={paciente.pasos}>
                          <Dato etiqueta={texto("pruebas.identificacion")} valor={paciente.identificacion} />
                          <Dato etiqueta={texto("pruebas.codigo")} valor={paciente.codigoAcceso} />
                          <Dato etiqueta={texto("pruebas.cita")} valor={diaDeCita(paciente.diasDesdeHoy, texto)} />
                          <Dato etiqueta={texto("pruebas.estado-inicial")} valor={texto(`pruebas.etapa.${paciente.etapa}`)} />
                        </Caso>
                      ))}
                    </Lista>
                  </Tabs.Panel>
                  <Tabs.Panel id="personal">
                    <Lista vacia={catalogo.personal.length === 0} texto={texto}>
                      {catalogo.personal.map((usuario) => (
                        <Caso key={usuario.usuario} titulo={usuario.nombre} pasos={[usuario.uso]}>
                          <Dato etiqueta={texto("pruebas.usuario")} valor={usuario.usuario} />
                          <Dato etiqueta={texto("pruebas.clave")} valor={usuario.clave} />
                          <Dato
                            etiqueta={texto("pruebas.modulo")}
                            valor={
                              <a href={usuario.modulo} className="inline-flex min-h-11 items-center text-action underline underline-offset-2 hover:text-action-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring">
                                {usuario.modulo}
                              </a>
                            }
                          />
                        </Caso>
                      ))}
                    </Lista>
                  </Tabs.Panel>
                  <Tabs.Panel id="callcenter">
                    <Lista vacia={catalogo.callcenter.length === 0} texto={texto}>
                      {catalogo.callcenter.map((paciente) => (
                        <Caso key={paciente.identificacion} titulo={paciente.identificacion} pasos={paciente.pasos}>
                          <Dato etiqueta={texto("pruebas.identificacion")} valor={paciente.identificacion} />
                          <Dato etiqueta={texto("pruebas.celular")} valor={paciente.celular} />
                        </Caso>
                      ))}
                    </Lista>
                  </Tabs.Panel>
                </Tabs>
              </Modal.Body>
              <Modal.Footer>
                <Button slot="close" variant="outline">
                  {texto("pruebas.cerrar")}
                </Button>
              </Modal.Footer>
            </Modal.Dialog>
          </Modal.Container>
        </Modal.Backdrop>
      </Modal>
    </section>
  );
}

function diaDeCita(dias: number, texto: Texto): string {
  if (dias === 0) return texto("pruebas.cita.hoy");
  if (dias === 1) return texto("pruebas.cita.manana");
  return conValores(texto("pruebas.cita.dias"), { dias });
}

function Lista({ vacia, texto, children }: { vacia: boolean; texto: Texto; children: ReactNode }) {
  if (vacia) return <p className="py-4 text-sm text-ink">{texto("pruebas.vacio")}</p>;
  return <ul className="flex flex-col divide-y divide-border">{children}</ul>;
}

function Caso({ titulo, pasos, children }: { titulo: string; pasos: readonly string[]; children: ReactNode }) {
  return (
    <li className="flex flex-col gap-2 py-4">
      <h3 className="text-base font-bold text-ink">{titulo}</h3>
      <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm sm:grid-cols-4">{children}</dl>
      {pasos.length === 1 ? (
        <p className="text-sm text-ink-muted">{pasos[0]}</p>
      ) : (
        <ol className="flex list-decimal flex-col gap-1 pl-5 text-sm text-ink-muted">
          {pasos.map((paso) => (
            <li key={paso}>{paso}</li>
          ))}
        </ol>
      )}
    </li>
  );
}

function Dato({ etiqueta, valor }: { etiqueta: string; valor: ReactNode }) {
  return (
    <div className="min-w-0">
      <dt className="text-ink-subtle">{etiqueta}</dt>
      <dd className="break-words font-medium text-ink tabular-nums select-all">{valor}</dd>
    </div>
  );
}
