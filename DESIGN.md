# DESIGN.md: sistema de diseño de la prefactura de consulta externa

Dirección visual del frontend (React + Tailwind CSS v4). Fuentes:
- Manual de marca del hospital: `_Segundo_Cerebro/00_Meta/manual-marca-hospital.pdf`, resumido en `_Segundo_Cerebro/03_Diseño_UI/manual-marca-resumen.md`.
- Portal institucional `www.hosusana.gov.co` (auditado el 2026-10-22).
- Modo Operate de Impeccable (pbakaus/impeccable).
- Reglas de pantallas de aplicación de taste-skill (leonxlnx/taste-skill).
- Filtro antislop (`.claude/skills/antislop/`), en modo during.

`AGENTS.md` sigue mandando sobre arquitectura y código; este archivo manda sobre lo visual.

## 1. Lectura de diseño

Reading this as: herramienta de trámite de un hospital público para pacientes y facturadores, en el lenguaje institucional del Hospital Susana López de Valencia (verdes y azul del manual, sobre blanco), dial ENERGY 1 / RHYTHM 1 / MOTION 1.

| Superficie | Usuario | Modo | Densidad |
|------------|---------|------|----------|
| Web del paciente | Paciente en celular o computador: entra con identificación + ID de acceso, diligencia datos, carga PDF, paga copago | Operate (trámite) | Baja. Un paso por pantalla en móvil. |
| Panel del facturador | Facturador de consulta externa: revisa solicitudes, registra novedades, aprueba, factura | Operate (herramienta de trabajo) | Alta. Tablas, filtros, detalle lateral. |

Dial: ENERGY 1 / RHYTHM 1 / MOTION 1. Es un servicio público de salud; la pantalla desaparece detrás de la tarea.

Motivo de identidad: la franja de bloques verdes de varios tonos que el manual pone bajo el encabezado en todas sus piezas (portada, carpeta, tarjetas, membrete). Se usa una sola vez por pantalla: una franja de 4 px bajo la barra superior. No se repite como decoración en tarjetas ni fondos.

## 2. Color

### 2.1 Colores institucionales (manual, página 11 del PDF)

| Token | HEX | Pantone | Uso permitido en UI | Contraste sobre blanco |
|-------|-----|---------|---------------------|------------------------|
| `brand-green-light` | `#7AB51D` | 368 C | Franja de identidad, rellenos sin texto, indicadores de progreso | 2,48:1. Nunca para texto ni iconos informativos. |
| `brand-green-dark` | `#2B722A` | 364 C | Franja de identidad, bordes de acento, texto de 18,66 px bold o más | 5,93:1. AA, no AAA para texto normal. |
| `brand-navy` | `#13235B` | 2758 C | Títulos, barra superior del panel, texto sobre fondos claros | 14,80:1. AAA. |

El logosímbolo usa siempre sus colores originales. Los tokens derivados de la sección 2.2 son de interfaz y nunca se aplican al logo.

### 2.2 Tokens de interfaz (objetivo WCAG AAA: 7:1 para texto normal)

| Token | HEX | Uso | Contraste medido |
|-------|-----|-----|------------------|
| `surface` | `#FFFFFF` | Fondo de contenido | n/a |
| `surface-2` | `#F4F6F3` | Segunda capa neutra: barra lateral, encabezados de panel. Gris levemente verde, tomado del verde de marca. | `ink` 15,24:1 |
| `ink` | `#1B1F24` | Texto principal | 16,56:1 |
| `ink-muted` | `#3D434B` | Texto secundario, etiquetas | 9,99:1; 9,19:1 sobre `surface-2` |
| `ink-subtle` | `#4A5059` | Metadatos, ayudas | 8,13:1 |
| `border` | `#D5D9D2` | Divisores de 1 px entre filas y secciones (decorativos) | 1,43:1; no delimita controles |
| `border-control` | `#7D837A` | Borde de campos, casillas y controles | 3,89:1 sobre `surface`, 3,58:1 sobre `surface-2` (WCAG 1.4.11 pide 3:1) |
| `action` | `#1E5520` | Botón primario (texto blanco), enlaces, foco | 8,84:1 (blanco sobre `action`) |
| `action-hover` | `#1A4D1C` | Hover y estado activo del botón primario | 9,90:1 |
| `focus-ring` | `#13235B` | Anillo de foco de 2 px con 2 px de separación | 14,80:1 |

`action` es una variante oscurecida de `brand-green-dark`. Hace falta porque el verde institucional con texto blanco da 5,93:1 y no alcanza AAA.

### 2.3 Estados de la solicitud

Los nombres salen de `_Segundo_Cerebro/01_Requerimientos/estados-prefactura.md`. Cada estado se muestra con texto y con icono, nunca solo con color.

| Estado del dominio | Equivale a | Texto | Fondo | Contraste |
|--------------------|-----------|-------|-------|-----------|
| EN_REVISION (P-04) | En revisión | `#1D3F8F` | `#E6ECF8` | 8,20:1 |
| PENDIENTE | Pendiente (tiene novedades) | `#6B4500` | `#FFF4D6` | 7,74:1 |
| APROBADO | Aprobado | `#1E5520` | `#E8F3DA` | 7,69:1 |
| LISTO PARA FACTURAR | Aprobado y con llegada confirmada | `#13235B` | `#E6ECF8` con borde `#13235B` | 12,48:1 |
| FACTURADO | Cerrado con factura | `#FFFFFF` | `#1E5520` | 8,84:1 |
| INGRESO ANULADO | Cerrado sin atención | `#4A4F57` | `#EEF0F2` | 7,22:1 |

"Rechazado" no es un estado en los insumos: una solicitud con problemas pasa a PENDIENTE con novedades. El rojo queda reservado para errores de validación y fallos del sistema:

| Token | Texto | Fondo | Contraste |
|-------|-------|-------|-----------|
| `danger` | `#8A1C1C` | `#FCE8E8` | 7,88:1 |

## 3. Tipografía

- El manual define Arial Rounded MT Bold solo para el logo e impresos. Es una fuente comercial sin licencia web y no se usa en la interfaz.
- La interfaz usa DM Sans, la fuente de cuerpo del portal, con licencia SIL OFL. Es una sola familia para títulos, etiquetas, datos y cuerpo, como pide el modo Operate.
- Montserrat (la fuente de títulos del portal) no se carga. Una segunda familia en una herramienta de trabajo no aporta y agrega peso.
- Cifras en tablas: `font-variant-numeric: tabular-nums`.

Escala fija en rem (razón ~1,125-1,2, sin `clamp`):

| Token | Tamaño / interlineado | Uso |
|-------|-----------------------|-----|
| `text-xs` | 12 / 16 px | Solo metadatos no esenciales en el panel |
| `text-sm` | 14 / 20 px | Base del panel del facturador, celdas de tabla |
| `text-base` | 16 / 24 px | Base de la web del paciente; todos los `input` (evita el zoom en iOS) |
| `text-lg` | 18 / 28 px | Títulos de sección |
| `text-xl` | 20 / 28 px | Título de pantalla en el panel |
| `text-2xl` | 24 / 32 px | Título de pantalla en la web del paciente |

Pesos: 400 cuerpo, 500 etiquetas y celdas destacadas, 700 títulos. Líneas de prosa entre 65 y 75 caracteres.

## 4. Espaciado, forma y superficie
- Base de 4 px. Pasos: 4, 8, 12, 16, 24, 32, 48.
- Radio único de 6 px para campos, botones, chips y paneles. Las pastillas de estado usan 999 px.
- Superficies planas: separación con borde de 1 px `border` o con cambio de tono a `surface-2`. Sin sombras. La única excepción es la capa flotante (menú desplegable, diálogo): sombra `0 4px 16px rgb(19 35 91 / 0.12)`.
- Sin degradados, sin glassmorphism, sin brillos.

## 5. Composición

### 5.1 Encabezado (las dos superficies)
- Logo horizontal oficial (`_Segundo_Cerebro/03_Diseño_UI/assets/logo-hospital-sin-acreditacion.png`) arriba a la izquierda, como el portal y el ejemplo del manual. Mínimo en pantalla: 153 × 43 px; en móvil, 96,4 × 25,6 px. Sin recolorear, sin efectos, con área de reserva libre.
- Franja de identidad de 4 px bajo el encabezado.
- La barra GOV.CO no se muestra en `hosusana.com`. Se agrega solo si el producto se publica bajo `.gov.co` (ver `_Segundo_Cerebro/02_Arquitectura/dictamen-viabilidad-frontend.md`).

### 5.2 Web del paciente
- Una columna, máximo 640 px de ancho, móvil primero.
- Pasos visibles: Identificación → Datos → Soportes PDF → Copago (solo si aplica) → Confirmación con consecutivo de radicado.
- Una acción primaria por pantalla.
- Carga de PDF con estado por archivo: en cola, cargando (barra de progreso), cargado, error con causa (no es PDF, tamaño).
- La confirmación muestra el consecutivo de radicado en texto grande y seleccionable.

### 5.3 Panel del facturador
- Barra superior `brand-navy` con el logo en negativo sobre área blanca o la versión blanca del manual. Contenido sobre `surface`.
- Lista de solicitudes en tabla: filas de 40 px, encabezado sin banda de color, números alineados a la derecha, sin filas cebra, estado en pastilla.
- Filtros sobre la tabla: estado, fecha de cita, texto (identificación o radicado).
- Detalle en panel lateral derecho: datos del paciente, soportes PDF visibles en línea, checklist de validación, registro de novedades, acciones Aprobar o Registrar novedad.
- Sin KPIs ni gráficos en el PoC. Ninguna nota de requerimientos pide métricas, y antislop R-38 prohíbe cifras inventadas.

### 5.4 Estados de componente
Todo control interactivo tiene default, hover, focus visible, active, disabled, loading y error. La carga usa esqueletos, no spinners en mitad del contenido. Los estados vacíos explican qué hacer ("No hay solicitudes radicadas para esta fecha").

## 6. Movimiento (MOTION 1)
- Solo transiciones de estado: hover, foco, apertura de panel lateral, progreso de carga, confirmación de guardado.
- Duración de 150 a 250 ms, curva `ease-out`. Sin secuencias de entrada de página, sin parallax, sin efectos al hacer scroll.
- `prefers-reduced-motion: reduce` desactiva toda animación no esencial.

## 7. Librerías autorizadas y uso permitido

Versiones y licencias consultadas el 2026-10-22.

| Librería | Qué es | Licencia | Uso en este proyecto |
|----------|--------|----------|----------------------|
| HeroUI v3 (`@heroui/react` 3.2.6) | Componentes React sobre React Aria y Tailwind v4 | MIT | Base de componentes de las dos superficies: botones, campos, tablas, tabs, diálogos. Requiere React 19 y Tailwind 4. Su tema se configura con los tokens de la sección 2. |
| Spectrum UI (ui.spectrumhq.in) | Bloques copy-paste con shadcn, Tailwind y Motion | Apache-2.0 | Opcional, para un bloque concreto que HeroUI no cubra. Se adapta a los tokens; no se mezcla su estilo con el de HeroUI. |
| Anime.js 4.5.0 | Motor de animación JavaScript | MIT | Solo para animaciones de estado que CSS no resuelva (por ejemplo, progreso de carga de varios archivos). Respeta MOTION 1. |
| AnimateIcons (animateicons.in) | Iconos animados basados en Lucide | MIT | Familia de iconos del proyecto junto con `lucide-react` (ISC). Animación solo al pasar el cursor sobre acciones. |
| Iconimate (iconimate.app) | Iconos animados basados en Phosphor | MIT | No se usa por defecto: mezclar Phosphor con Lucide rompe la consistencia de iconos (antislop R-04). |
| It's Hover (itshover.com) | Iconos animados al pasar el cursor | Apache-2.0 | Igual que Iconimate: solo si un icono falta en Lucide y su trazo coincide. |
| React Bits (reactbits.dev) | Componentes animados y fondos decorativos | MIT + Commons Clause | No se usa en superficies Operate: sus componentes son decorativos. La Commons Clause prohíbe vender el software. |
| Rare UI (rareui.com) | Componentes animados con shadcn y Motion | MIT + Commons Clause + atribución | No se usa por defecto. Si se usa, exige atribución y tiene la misma restricción de venta. |

Referencias de criterio (no son dependencias):

| Repositorio | Licencia | Qué se toma |
|-------------|----------|-------------|
| miqdadbadjuber/anti-slop | MIT | Filtro activo en modo during; Delivery Gate antes de entregar cada pantalla. Instalado en `.claude/skills/`. |
| pbakaus/impeccable (modo Operate) | Apache-2.0 | Una familia tipográfica, escala fija, paleta restringida con estados semánticos, componentes con todos sus estados, movimiento de 150 a 250 ms. |
| leonxlnx/taste-skill (app screens) | MIT | Superficies planas, tablas por reglas y alineación, pastillas de estado, sin KPIs decorativos ni cifras inventadas. |
| nextlevelbuilder/ui-ux-pro-max-skill | MIT | Catálogo consultable de guías UX y accesibilidad para revisiones puntuales. El repositorio citado como `ui-ux-pro-max` no existe; el correcto es `ui-ux-pro-max-skill`. |

## 8. Accesibilidad
- Texto normal a 7:1 o más, texto grande a 4,5:1 o más (AAA). Valores medidos en la sección 2.
- Foco visible en todo control, con `focus-ring`.
- Navegación completa con teclado en el panel: tabla, panel lateral y acciones.
- Campos con etiqueta visible; los errores se asocian al campo (`aria-describedby`) y se anuncian.
- Objetivos táctiles de 44 × 44 px o más en la web del paciente.
- `lang="es-CO"`.

## 9. Contenido
- Textos con las reglas de `.claude/rules/redaccion-anti-slop.md` y la skill `antislop-copywriting`.
- Términos del dominio tal como aparecen en los requerimientos: "radicado", "novedad", "soporte", "copago", "ingreso", "digiturno".
- Datos de prueba ficticios y marcados como tales. Ningún dato real de pacientes en el PoC.

## 10. Tokens para Tailwind v4

```css
@theme {
  --color-brand-green-light: #7AB51D;
  --color-brand-green-dark: #2B722A;
  --color-brand-navy: #13235B;

  --color-surface: #FFFFFF;
  --color-surface-2: #F4F6F3;
  --color-ink: #1B1F24;
  --color-ink-muted: #3D434B;
  --color-ink-subtle: #4A5059;
  --color-border: #D5D9D2;
  --color-border-control: #7D837A;
  --color-action: #1E5520;
  --color-action-hover: #1A4D1C;
  --color-focus-ring: #13235B;

  --color-state-radicado-fg: #1D3F8F;    --color-state-radicado-bg: #E6ECF8;
  --color-state-pendiente-fg: #6B4500;   --color-state-pendiente-bg: #FFF4D6;
  --color-state-aprobado-fg: #1E5520;    --color-state-aprobado-bg: #E8F3DA;
  --color-state-listo-fg: #13235B;       --color-state-listo-bg: #E6ECF8;
  --color-state-facturado-fg: #FFFFFF;   --color-state-facturado-bg: #1E5520;
  --color-state-anulado-fg: #4A4F57;     --color-state-anulado-bg: #EEF0F2;
  --color-danger-fg: #8A1C1C;            --color-danger-bg: #FCE8E8;

  --font-sans: "DM Sans", system-ui, sans-serif;
  --radius-control: 6px;
  --ease-state: cubic-bezier(0, 0, 0.2, 1);
  --duration-state: 200ms;
}
```
