// Verificaciones de AGENTS.md que corren con `gradlew check`.

val limiteLineasPorTipo = 30
val declaracionDeTipo = Regex("""(^|[\s(])(class|interface|object)(\s|\{|:|$)""")

// Quita comentarios y KDoc; las cadenas se reducen a "" para que sus llaves no cuenten.
fun codigoSinComentarios(fuente: String): List<String> {
    val salida = StringBuilder()
    var i = 0
    fun saltarHasta(fin: Int, marcaPorLinea: String) {
        fuente.substring(i, fin).forEach { if (it == '\n') salida.append('\n').append(marcaPorLinea) }
        i = fin
    }
    while (i < fuente.length) {
        val c = fuente[i]
        when {
            fuente.startsWith("/*", i) -> saltarHasta(fuente.indexOf("*/", i + 2).let { if (it < 0) fuente.length else it + 2 }, "")
            fuente.startsWith("//", i) -> i = fuente.indexOf('\n', i).let { if (it < 0) fuente.length else it }
            fuente.startsWith("\"\"\"", i) -> {
                salida.append("\"\"")
                saltarHasta(fuente.indexOf("\"\"\"", i + 3).let { if (it < 0) fuente.length else it + 3 }, "\"\"")
            }
            c == '"' || c == '\'' -> {
                var j = i + 1
                while (j < fuente.length && fuente[j] != c) j += if (fuente[j] == '\\') 2 else 1
                salida.append(c).append(c)
                i = j + 1
            }
            else -> { salida.append(c); i++ }
        }
    }
    return salida.lines()
}

fun lineasDelTipo(lineas: List<String>, inicio: Int): Int {
    var llaves = 0
    var parentesis = 0
    var abrioCuerpo = false
    var total = 0
    for (n in inicio until lineas.size) {
        val linea = lineas[n]
        if (linea.isNotBlank()) total++
        linea.forEach {
            when (it) {
                '{' -> { llaves++; abrioCuerpo = true }
                '}' -> llaves--
                '(' -> parentesis++
                ')' -> parentesis--
            }
        }
        if (abrioCuerpo && llaves <= 0) return total
        val siguiente = lineas.drop(n + 1).firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        val continua = linea.trimEnd().endsWith(",") || siguiente.take(1) in setOf("{", ":", ")")
        if (!abrioCuerpo && parentesis <= 0 && !continua) return total
    }
    return total
}

val fuentesKotlin = fileTree("src") { include("**/*.kt") }

val verificarLimiteClases = tasks.register("verificarLimiteClases") {
    group = "verification"
    description = "Falla si una clase, object o interface supera $limiteLineasPorTipo líneas de código."
    inputs.files(fuentesKotlin)
    doLast {
        val violaciones = fuentesKotlin.files.sorted().flatMap { archivo ->
            val lineas = codigoSinComentarios(archivo.readText())
            lineas.indices.filter { declaracionDeTipo.containsMatchIn(lineas[it]) }
                .map { it to lineasDelTipo(lineas, it) }
                .filter { (_, total) -> total > limiteLineasPorTipo }
                .map { (inicio, total) -> "${archivo.relativeTo(rootDir)}:${inicio + 1} tiene $total líneas" }
        }
        if (violaciones.isNotEmpty()) throw GradleException(violaciones.joinToString("\n"))
        logger.lifecycle("Límite de $limiteLineasPorTipo líneas: ${fuentesKotlin.files.size} archivos sin violaciones")
    }
}

// Caracteres invisibles que watermarks-remover (capa A) trata como marcas o artefactos.
val rangosInvisibles = listOf(
    0x00AD..0x00AD, 0x034F..0x034F, 0x180B..0x180F, 0x200B..0x200F, 0x202A..0x202E,
    0x2060..0x2064, 0x2066..0x206F, 0xFE00..0xFE0F, 0xFEFF..0xFEFF, 0xFFF9..0xFFFB,
    0xE000..0xF8FF, 0xE0000..0xE0FFF,
)

val archivosDeTexto = fileTree(rootDir) {
    include("src/**", "config/**", "gradle/**", "*.kts", "*.md", "_Segundo_Cerebro/**/*.md")
    exclude("**/*.png", "**/*.jpg", "**/*.jpeg", "**/*.jar")
}

val verificarCaracteresInvisibles = tasks.register("verificarCaracteresInvisibles") {
    group = "verification"
    description = "Falla si un archivo de texto del proyecto contiene caracteres invisibles."
    inputs.files(archivosDeTexto)
    doLast {
        val hallazgos = archivosDeTexto.files.sorted().flatMap { archivo ->
            archivo.readLines().withIndex().flatMap { (n, linea) ->
                linea.codePoints().toArray().filter { cp -> rangosInvisibles.any { cp in it } }
                    .map { "${archivo.relativeTo(rootDir)}:${n + 1} U+%04X".format(it) }
            }
        }
        if (hallazgos.isNotEmpty()) throw GradleException(hallazgos.joinToString("\n"))
        logger.lifecycle("Caracteres invisibles: ${archivosDeTexto.files.size} archivos limpios")
    }
}

tasks.named("check") {
    dependsOn(verificarLimiteClases, verificarCaracteresInvisibles)
}
