import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";

afterEach(cleanup);

// jsdom no trae ResizeObserver; el cuerpo desplazable del modal de HeroUI lo usa para sus sombras de desplazamiento.
globalThis.ResizeObserver ??= class {
  observe() {}
  unobserve() {}
  disconnect() {}
};

// El indicador de las pestañas anima con la Web Animations API, que jsdom tampoco trae.
Element.prototype.getAnimations ??= () => [];
