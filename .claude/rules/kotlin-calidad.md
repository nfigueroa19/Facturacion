---
paths:
  - "**/*.kt"
  - "**/*.kts"
---

# Calidad de código Kotlin

Complementa `AGENTS.md`. Fuentes: skills `thermo-nuclear-code-quality-review` y `deslop` (cursor/plugins, cursor-team-kit), instaladas en `.claude/skills/`.

## Revisión obligatoria
Después de generar o modificar código Kotlin, y antes de proponer un commit o PR, ejecuta la skill `thermo-nuclear-code-quality-review` sobre el diff, luego `deslop` y, si el diff toca comentarios, `antislop-code`. Reporta los hallazgos estructurales primero.

## Bloqueantes
- Clase con más de 30 líneas de código (regla de conteo en `AGENTS.md`).
- Dependencia instanciada dentro de una clase (`= Foo()`, `object` singleton con estado, service locator). Las dependencias llegan por constructor como interfaces.
- Capa `domain` que importa algo de `data`, `service`, `api`, frameworks, drivers o librerías de serialización/persistencia.
- Condicionales ad hoc agregados a un flujo existente para cubrir un caso especial. Modela el caso (sealed class, estrategia, política) en lugar de ramificar.
- Cambio de comportamiento sin prueba que lo cubra.
- Estados de negocio representados como `String` o `Int` sueltos en lugar de `enum`/`sealed`.
- `!!`, casts inseguros (`as`) o nulabilidad que oculta una invariante del dominio.
- Wrappers o capas de paso que no agregan claridad.
- Lógica duplicada que ya existe en otra clase.

## Slop de código a eliminar
- Comentarios redundantes o que narran el código.
- `try/catch` defensivos en rutas confiables. Los errores de negocio se modelan como tipos (`sealed class Resultado`), no como excepciones genéricas.
- Anidamiento profundo que se resuelve con retornos tempranos.
