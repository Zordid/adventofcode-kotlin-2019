import java.io.File
import java.net.URL
import java.util.*

abstract class Day<T>(day: Int, year: Int, processor: (String) -> T) {
    val input = getInput(day, year, processor)

    open fun part1(): Any? = input
    open fun part2(): Any? = input
}

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
    getInput(day, year) { it.toInt() }

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

fun main() {
    val x = getInput() { it }
    println(x)
}