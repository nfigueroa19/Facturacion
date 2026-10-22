import { Skeleton } from "@heroui/react";

/** Reproduce la forma de la pantalla de acceso para que el contenido no salte al llegar los textos. */
export function EsqueletoAcceso() {
  return (
    <div role="status" aria-live="polite" className="flex flex-col gap-6">
      <span className="sr-only">Cargando la prefactura</span>
      <Skeleton className="h-8 w-3/4 rounded-lg" />
      {[0, 1].map((campo) => (
        <div key={campo} className="flex flex-col gap-2">
          <Skeleton className="h-5 w-40 rounded-lg" />
          <Skeleton className="h-11 w-full rounded-lg" />
        </div>
      ))}
      <Skeleton className="h-11 w-full rounded-lg sm:w-32" />
    </div>
  );
}
