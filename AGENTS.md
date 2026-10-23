# AGENTS.md: arquitectura, calidad y control del agente

Reglas técnicas obligatorias para cualquier agente (Claude Code u otro) que genere o modifique código en este proyecto. Las convenciones de la bóveda de conocimiento están en `CLAUDE.md`.

## 1. Arquitectura: Clean Architecture en Kotlin

### Capas
La estructura `src/domain`, `src/data`, `src/service`, `src/api` es una guía de separación de capas. Los nombres de clases y paquetes salen de los modelos reales documentados en `_Segundo_Cerebro/01_Requerimientos/`, no de ejemplos genéricos.

| Capa | Contenido | Puede depender de |
|------|-----------|-------------------|
| `src/domain` | Entidades, value objects, estados (`sealed`/`enum`), reglas de negocio puras e interfaces de repositorio y de puertos externos. | Nada. Solo la librería estándar de Kotlin. |
| `src/service` | Casos de uso: orquestan entidades y puertos del dominio. Un caso de uso por clase. | `domain` |
| `src/data` | Implementaciones de repositorios y puertos: base de datos, clientes HTTP, almacenamiento de soportes PDF, mensajería, adaptadores a sistemas externos. | `domain` |
| `src/api` | Controladores REST, DTOs de entrada/salida, mapeo DTO a dominio, validación de formato. | `service`, `domain` |

La composición (registro de dependencias) vive en un módulo de arranque separado. Es el único lugar donde se instancian implementaciones concretas.

### Reglas de dependencia
- Las dependencias apuntan hacia `domain`. `domain` no importa frameworks, ORM, serializadores ni clientes HTTP.
- Los DTOs de `api` y las entidades de persistencia de `data` no cruzan a `domain`. Se mapean en el borde.
- Cada integración externa (pasarela de pagos, SMS, aplicativo de facturación, digiturno) es un puerto (interfaz) en `domain` y un adaptador en `data`.

### Modelo del dominio
Ver sección 5. Los nombres de entidades y estados se toman de las notas de requerimientos; si un concepto no está documentado allí, el agente pregunta antes de crearlo.

## 2. SOLID y límites de tamaño

### Responsabilidad única (SRP)
Cada clase tiene una sola razón para cambiar. Un caso de uso, un adaptador, un controlador por recurso.

### Límite de 30 líneas por clase
- Ninguna clase, `object` o `interface` supera 30 líneas de código.
- Cuentan las líneas desde la declaración de la clase hasta su llave de cierre. No cuentan líneas en blanco, comentarios, KDoc, `import` ni `package`.
- Si una clase supera el límite, se refactoriza antes de entregar: extraer colaboradores, delegados (`by`), funciones de extensión puras o casos de uso separados.
- No se cumple el límite comprimiendo código (varias sentencias por línea, expresiones ilegibles).

### Inyección de dependencias
- Toda dependencia llega por constructor y tiene tipo de interfaz.
- Prohibido instanciar colaboradores dentro de una clase (`private val repo = RepoImpl()`), usar singletons con estado o service locators.
- Excepción: value objects y data classes del dominio se crean con constructor directo.

### Abierto/cerrado (OCP)
- Un nuevo canal o integración se agrega con una clase nueva que implementa una interfaz existente.
- Los comportamientos que varían por tipo (por ejemplo, régimen o tipo de soporte) se modelan con `sealed class` o estrategias, no con cadenas de `if`/`when` repartidas por el código.

### Sustitución, segregación e inversión
- Interfaces pequeñas y específicas por consumidor (ISP).
- Cualquier implementación de un puerto es intercambiable sin cambiar el caso de uso (LSP).
- Los casos de uso dependen de abstracciones del dominio, nunca de `data` (DIP).

### Pruebas
- Todo caso de uso y toda regla de negocio tiene prueba unitaria con dobles de los puertos.
- Las reglas con fecha (por ejemplo, facturación el mismo día de la cita) reciben el reloj como dependencia (`java.time.Clock` o un puerto propio), nunca leen la hora del sistema directamente.

## 3. Restricciones de seguridad para el agente

- **Alcance.** El agente modifica solo los archivos que pide el prompt activo. Para tocar cualquier otro archivo o clase, se detiene y pide confirmación expresa.
- **Contratos públicos.** Sin confirmación previa, el agente no renombra métodos públicos, no cambia firmas, no altera endpoints, rutas, DTOs públicos ni middlewares existentes.
- **Cambios colaterales.** Si un cambio exige editar otro archivo, el agente se detiene e indica: archivo, cambio exacto y motivo. Espera la respuesta antes de continuar.
- **Reglas de negocio.** El agente no inventa reglas hospitalarias, normativas (DIAN, Ministerio de Salud, EPS) ni datos de contratos. Si falta un dato, pregunta y deja la pregunta registrada en la nota de requerimientos correspondiente.
- **Datos sensibles.** Datos de pacientes, soportes y autorizaciones no se envían a APIs externas ni se escriben en logs. No se usan datos reales en pruebas ni ejemplos.
  - **Excepción PoC (autorizada por el usuario el 2026-10-22).** Durante la prueba piloto la persistencia del backend (tablas y soportes PDF) vive en Appwrite Cloud. Solo se cargan datos ficticios de pacientes, citas y soportes, como los de los diagramas y el audio de requerimientos. Cargar datos reales exige retirar esta excepción y una nueva decisión de infraestructura. La prohibición de escribir datos de pacientes en logs sigue vigente.

## 4. Calidad: revisión estricta y anti-slop

### Revisión de código (Thermo-Nuclear)
Después de generar o modificar código y antes de proponer commit o PR, el agente ejecuta la skill `thermo-nuclear-code-quality-review` (fuente: cursor/plugins, cursor-team-kit) sobre el diff y luego `deslop`. Busca, en este orden:
1. Regresiones estructurales y acoplamiento implícito entre capas.
2. Simplificaciones que eliminan ramas, capas o conceptos completos.
3. Condicionales ad hoc agregados a flujos existentes.
4. Contratos de tipos difusos: `!!`, casts, nulabilidad que oculta invariantes, estados como `String`.
5. Clases de más de 30 líneas y métodos largos.
6. Lógica sin pruebas.

No se aprueba un cambio solo porque funciona. Reglas detalladas en `.claude/rules/kotlin-calidad.md`.

### Redacción (Humanizer / Stop-Slop / Anti-Slop)
Documentación, comentarios, commits, PRs y respuestas son directos, técnicos y concisos: sin muletillas de IA, sin texto inflado, sin disculpas ni rodeos. Reglas detalladas en `.claude/rules/redaccion-anti-slop.md`; skills `humanizer` y `stop-slop` para revisar textos largos.

### Interfaz, textos de UI y comentarios (antislop)
Fuente: miqdadbadjuber/anti-slop v3.2.21. Es un filtro, no una guía de estilo: rechaza técnicas sin propósito y no define colores ni tipografía. La dirección visual sale de `DESIGN.md` o de `_Segundo_Cerebro/03_Diseño_UI/`, nunca la inventa el agente.

<!-- antislop:start -->
## antislop
For UI, copy, people, mobile layout, or code comments work, read these installed skill files directly (use these paths even if a same-named global skill exists):
- Core filter, always on: `antislop`: `.claude/skills/antislop/SKILL.md`
- UI / visual: `antislop-ui`: `.claude/skills/antislop-ui/SKILL.md`
- Copy & text: `antislop-copywriting`: `.claude/skills/antislop-copywriting/SKILL.md`
- People: `antislop-human`: `.claude/skills/antislop-human/SKILL.md`
- Mobile / responsive: `antislop-layoutmobile`: `.claude/skills/antislop-layoutmobile/SKILL.md`
- Code comments: `antislop-code`: `.claude/skills/antislop-code/SKILL.md`
Before starting, follow the core's "Two Usage Modes" section in strict order: explicit session instruction first, then global preference, then ask. A session instruction always wins. For a resolved mode, say `antislop active: <mode> (session override).` or `antislop active: <mode> (global preference).` once before presenting findings or making edits, using the actual mode and source. Acknowledging the user's request without naming the source does not replace this notice.
Only an explicit choice of antislop during or after selects a session mode. A request to review, audit, or avoid file edits does not select a mode; read the global preference in that case. Another skill's mode does not select antislop's mode.
If the mode is unresolved, ask during/after and end the response; wait for the answer before any UI review, planning, or concept. For read-only tasks, put the active-mode notice only at the start of the final answer, never in progress messages. For editing tasks, announce before the first edit and omit it from the final answer.
To update antislop later: `npx antislop-ai --update`, or run `npx antislop-ai` and pick Overwrite them.
<!-- antislop:end -->

## 5. Modelo del dominio (prefactura de consulta externa)

Fuente: `_Segundo_Cerebro/01_Requerimientos/` (entidades, estados, reglas RN-01 a RN-15). Esta tabla guía la ubicación por capa; no autoriza a crear atributos ni reglas que no estén en esas notas.

| Concepto | Capa | Forma en Kotlin |
|----------|------|-----------------|
| Paciente, Cita, Médico, SolicitudPrefactura, Soporte, Novedad, Ingreso, Factura | `domain` | Entidades y value objects |
| Estado de la prefactura (PENDIENTE, APROBADO, INGRESO ANULADO, LISTO PARA FACTURAR, FACTURADO) | `domain` | `sealed interface` o `enum`; transiciones validadas en el dominio |
| Régimen y regla de copago | `domain` | `enum` + política de copago |
| Tipo de soporte (autorización, orden médica) y formato PDF | `domain` | `enum` + value object de archivo validado |
| Regla de facturación el mismo día de la cita (RN-01) | `domain` | Política que recibe el reloj como dependencia |
| Repositorios de solicitudes, soportes y novedades | `domain` (interfaz) / `data` (implementación) | Puertos |
| Pasarela de pagos, SMS, digiturno, aplicativo de facturación (Dinámica) | `domain` (puerto) / `data` (adaptador) | Un adaptador por proveedor. Dinámica inicia con adaptador manual |
| Radicar solicitud, validar, registrar novedad, confirmar llegada, anular ingreso, registrar factura | `service` | Un caso de uso por clase |
| Autenticación con identificación + ID de acceso, carga de soportes, panel del facturador | `api` | Controladores y DTOs |

Datos de pacientes: se tratan como sensibles en todas las capas (sección 3).
