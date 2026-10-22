# Redacción anti-slop

Aplica a notas de la bóveda, documentación, mensajes de commit, descripciones de PR, respuestas al usuario y comentarios en código.

Fuentes: skills `humanizer` (blader/humanizer), `stop-slop` (hardikpandya/stop-slop) y `antislop-copywriting` (miqdadbadjuber/anti-slop), instaladas en `.claude/skills/`. Para revisar un texto largo, invoca la skill `humanizer`. Para textos de interfaz (títulos, botones, mensajes), usa `antislop-copywriting`.

## Reglas
- Escribe la afirmación concreta. Nombra el dato, la entidad, el estado o el archivo.
- Elimina aperturas de relleno: "Cabe destacar", "Es importante mencionar", "A continuación", "Vamos a", "En resumen", "Sin duda".
- Elimina contrastes inflados: "No es solo X, es Y", "Más que X, es Y". Escribe Y.
- Elimina cierres de una línea que repiten el párrafo anterior.
- Evita vocabulario inflado: crucial, clave (como adjetivo), robusto (en sentido figurado), integral, potenciar, sinergia, ecosistema, fundamental, innovador.
- Usa "es", "tiene", "está". Evita "funge como", "se erige como", "cuenta con" cuando sobran.
- Sin raya (—) ni semiraya (–) como conector. Usa punto, coma, dos puntos o paréntesis.
- Sin negritas decorativas ni emojis en títulos. Única excepción: el encabezado obligatorio `## 🔗 Relacionado` definido en `CLAUDE.md`.
- Sin disculpas, saludos, ofrecimientos ni cierres de chatbot ("Espero que sirva", "¿Quieres que...?").
- Sin triadas forzadas. Dos elementos reales valen más que tres inventados.
- No agregues hechos, cifras, normas ni nombres que no estén en las fuentes. Si falta un dato, márcalo como pregunta abierta.

## Comentarios en código
- Comenta el porqué cuando no es evidente. No narres lo que el código ya dice.
- Sin comentarios que describan cambios ("se agregó para reemplazar..."). Eso va en el commit.
