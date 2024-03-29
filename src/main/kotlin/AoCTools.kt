import java.io.File
import java.net.URL
import java.util.*
import kotlin.system.measureTimeMillis

abstract class Day<T>(
    private val day: Int = today,
    private val year: Int = thisYear,
    processor: (String) -> T,
    testData: List<String>? = null
) {
    val input: List<T> = testData?.map(processor) ?: getInput(day, year, processor)

    open fun part1(): Any? = "TODO"
    open fun part2(): Any? = "TODO"

    private fun maxWidth(l: String) =
        if (l.length <= 50) l else
            "${l.substring(0, 50)}... (${l.length} total)"

    private fun showInputHint() {
        println("= INPUT ===================")
        input.take(5).forEachIndexed { idx, l ->
            println("${idx + 1}: ${maxWidth(l.toString())}")
        }
        if (input.size > 5) println("... (${input.size} total)")
        println("===========================")
    }

    private inline fun runWithTiming(part: Int, f: () -> Any?) {
        var result: Any? = null
        val millis = measureTimeMillis { result = f() }
        val duration = if (millis < 1000) "$millis ms" else "${"%.3f".format(millis / 1000.0)} s"
        println("Solution $part: (took $duration)\n$result")
    }

    fun run(vararg parts: Int = IntArray(2) { it + 1 }) {
        println("=== AoC $year, day $day ===")
        showInputHint()

        if (1 in parts)
            runWithTiming(1, ::part1)
        if (2 in parts)
            runWithTiming(2, ::part2)
    }
}

fun List<String>.justInts(separator: String = ",") = first().split(separator).map { it.toInt() }
fun List<String>.justLongs(separator: String = ",") = first().split(separator).map { it.toLong() }

fun asStrings(s: String) = s
fun asInts(s: String) = s.toInt()
fun asLongs(s: String) = s.toLong()

val today: Int
    get() = with(Calendar.getInstance()) {
        time = Date()
        get(Calendar.DAY_OF_MONTH)
    }

val thisYear: Int
    get() = with(Calendar.getInstance()) {
        time = Date()
        get(Calendar.YEAR)
    }

fun getInputAsInt(day: Int = today, year: Int = thisYear): Int =
    getInputAsString(day, year).toInt()

fun getInputAsLong(day: Int = today, year: Int = thisYear): Long =
    getInputAsString(day, year).toLong()

fun getInputAsString(day: Int = today, year: Int = thisYear): String =
    getInputAsStrings(day, year).joinToString("")

fun getInputAsStrings(day: Int = today, year: Int = thisYear): List<String> =
    getInput(day, year) { it }

fun getInputAsInts(day: Int = today, year: Int = thisYear): List<Int> =
    getInput(day, year) { it }.let { if (it.size == 1) it[0].split(",").map { it.toInt() } else it.map { it.toInt() } }

fun getInputAsLongs(day: Int = today, year: Int = thisYear): List<Long> =
    getInput(
        day,
        year
    ) { it }.let { if (it.size == 1) it[0].split(",").map { it.toLong() } else it.map { it.toLong() } }

fun <T> getInput(day: Int = today, year: Int = thisYear, mapper: (String) -> T): List<T> {
    val cached = readInput(day, year, mapper)
    if (cached != null) return cached

    val lines = downloadInput(day, year)
    storeInput(day, year, lines)

    return lines.map(mapper)
}

private fun downloadInput(day: Int, year: Int): List<String> {
    println("Downloading puzzle for $year, day $day...")
    val uri = "https://adventofcode.com/$year/day/$day/input"
    val cookies = mapOf("session" to getSessionCookie())

    val url = URL(uri)
    val connection = url.openConnection()
    connection.setRequestProperty(
        "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
    )
    connection.connect()
    val result = arrayListOf<String>()
    connection.getInputStream().bufferedReader().useLines { result.addAll(it) }
    return result
}

private fun getSessionCookie() =
    System.getenv("AOC_COOKIE") ?: object {}.javaClass.getResource("session-cookie").readText()

private fun <T> readInput(day: Int, year: Int, mapper: (String) -> T): List<T>? {
    val file = File(fileNameFor(day, year))
    file.exists() || return null
    val result = arrayListOf<T>()
    file.bufferedReader().useLines { lines -> lines.map(mapper).forEach { result += it } }
    return result
}

private fun storeInput(day: Int, year: Int, puzzle: List<String>) {
    File(pathNameForYear(year)).mkdirs()
    File(fileNameFor(day, year)).writeText(puzzle.joinToString("\n"))
}

fun pathNameForYear(year: Int) = "puzzles/$year"
fun fileNameFor(day: Int, year: Int) = "${pathNameForYear(year)}/day${"%02d".format(day)}.txt"

fun measureAverageMillis(count: Int, block: (Int) -> Unit) =
    (measureTimeMillis { repeat(count, block) } - measureTimeMillis { repeat(count) {} }).toDouble() / count
