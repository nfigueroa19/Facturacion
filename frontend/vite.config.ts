/// <reference types="vitest/config" />
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// API del PoC en Render. Solo se usa con `--mode cloudflare` (`npm run deploy`); en desarrollo sigue el Ktor local.
const URL_API_POC = "https://api-facturacion.hosusana.com";

export default defineConfig(({ mode }) => ({
  plugins: [react(), tailwindcss()],
  define: mode === "cloudflare" ? { "import.meta.env.VITE_URL_API": JSON.stringify(URL_API_POC) } : {},
  // El backend solo acepta CORS desde http://localhost:5173 (HSLV_CORS_ORIGENES).
  server: { host: "localhost", port: 5173, strictPort: true },
  test: {
    environment: "jsdom",
    setupFiles: ["./src/pruebas/configuracion.ts"],
    css: false,
  },
}));
