/// <reference types="vitest/config" />
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  // El backend solo acepta CORS desde http://localhost:5173 (HSLV_CORS_ORIGENES).
  server: { host: "localhost", port: 5173, strictPort: true },
  test: {
    environment: "jsdom",
    setupFiles: ["./src/pruebas/configuracion.ts"],
    css: false,
  },
});
