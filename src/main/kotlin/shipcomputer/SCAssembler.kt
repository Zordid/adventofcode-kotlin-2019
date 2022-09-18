package shipcomputer

import IntcodeComputer
import kotlin.random.Random

val prog = """
     JNZ 0 23
     JZ 0 START

#DEFINE outer 100
#DEFINE inner 10

DATA:
     (0)
     #NOISE 100

START:
    ADD outer 0 [DATA]
OUTER:
    ADD RET 0 [STACK_PTR]
    ADD [STACK_PTR] -1 [STACK_PTR]
    JZ 0 FUN_PRINT

RET:
    OUT -9999
    ADD [DATA] -1 [DATA]
    JNZ [DATA] OUTER
    ADD 1 0 [0]
    HLT

FUN_PRINT:
    ADD inner 0 [X+1]
X:  OUT 9999
    ADD [X+1] -1 [X+1]
    JNZ [X+1] X
    ADD [STACK_PTR] 1 [STACK_PTR]
    JZ 0 [STACK_PTR]

STACK_PTR:
    (STACK_HIGH)
STACK_LOW:
    #NOISE 10
STACK_HIGH:

""".trimIndent()

val counter = """

    IN [START]
    IN [END]
    IN [STEP]

    ADD [START] 0 [I]
    ADD [END] 1 [END]

LOOP:
    LT [I] [END] [CMP]
    JZ [CMP] EXIT

    OUT [I]
    ADD [I] [STEP] [I]
    JZ 0 LOOP

EXIT:
    HLT

CMP: (0)
I: (0)
START: (0)
END: (0)
STEP: (0)
""".trimIndent()

class SCAssembler(code: String) {

    val intProgram: List<Long>
        get() = result

    private val lines = code.trim()
        .split(Regex("""\s*\n\s*"""))
        .filter { it.isNotBlank() && !it.startsWith("//") }
    private val result = mutableListOf<Long>()
    private val markers = mutableMapOf<String, Int>()
    private val unresolvedMarkers = mutableMapOf<String, MutableList<Int>>()

    private val instructionSet = IntcodeComputer(emptyList()).instructionSet

    init {
        lines.forEach { line ->
            val address = result.size
            markers["@THIS"] = address
            markers["_"] = Random.nextInt(1, 99)
            markers["?"] = Random.nextInt(1, 99)
            markers.remove("@NEXT")
            val command = processAndStripMarker(line)
            if (command.isNotBlank())
                println(line)
            when {
                command.isWrappedIn('(', ')') -> {
                    val literals = command.unwrap().split(Regex("\\s*,\\s*"))
                    literals.filter { it.isNotBlank() }.forEach { literal ->
                        result.add(evaluate(literal))
                    }
                }
                command.isWrappedIn('"', '"') -> {
                    command.unwrap().forEach { result.add(it.toLong()) }
                    result.add(0)
                }
                command.startsWith('#') -> {
                    val elements = command.split(Regex("\\s+"))
                    when (elements[0]) {
                        "#DEFINE" -> {
                            require(elements.size == 3) { "not enough parameters for define" }
                            markers[elements[1]] = elements[2].toInt()
                        }
                        "#NOISE" -> {
                            require(elements.size > 1) { "at least one parameter needed" }
                            val size = elements[1].toInt()
                            repeat(size) {
                                result.add(Random.nextLong(100))
                            }
                        }
                        "#NOISE_ALIGN_TO" -> {
                            require(elements.size > 1) { "at least one parameter needed" }
                            val align = elements[1].toInt()
                            while (result.size < align) {
                                result.add(Random.nextLong(100))
                            }
                        }
                        else -> error("Unknown directive $elements[0]")
                    }
                }
                command.isNotBlank() -> {
                    val elements = command.split(Regex("\\s+")).toMutableList()
                    val mnemonic = elements.first().toUpperCase()
                    val parameters = elements.slice(1 until elements.size)

                    //println("$address $mnemonic $parameters")

                    val instruction =
                        instructionSet.find { it.name == mnemonic } ?: error("Unknown assembly command $mnemonic")
                    require(parameters.size == instruction.parameters) {
                        "Wrong amount of parameters for $mnemonic: need ${instruction.parameters}, have: ${parameters.size}"
                    }
                    val modes = extractModes(parameters)

                    markers["@NEXT"] = address + 1 + instruction.parameters
                    val opcode = modes * 100 + instruction.opcode
                    result.add(opcode)

                    parameters.forEach { p ->
                        var parameter = if (p.isIndirectParameter()) p.unwrap() else p

                        if (parameter[0] == '?') {
                            require(result[address - 1] == result.size.toLong()) {
                                "? marker indicates previous instruction should write here to ${result.size}," +
                                        " but instead writes to ${result[address - 1]}"
                            }
                        }

                        val offset = when {
                            parameter.substring(1).contains('+') -> {
                                val (newParameter, offset) = parameter.split("+")
                                parameter = newParameter
                                offset.toInt()
                            }
                            parameter.substring(1).contains('-') -> {
                                val (newParameter, offset) = parameter.split("-")
                                parameter = newParameter
                                -offset.toInt()
                            }
                            else -> 0
                        }
                        result.add(evaluate(parameter) + offset)
                    }
                }
            }
            if (command.isNotBlank())
                println("$address: ${result.slice(address until result.size)}")
        }

        unresolvedMarkers.forEach { (marker, addresses) ->
            //println("Reverse resolving $marker at ${addresses.size} positions...")
            val address = markers[marker] ?: error("Forward reference to $marker not found.")
            addresses.forEach { result[it] = result[it] + address }
        }

        println("Assembly done. Produced ${result.size} ints.")
        println("Created program: ${result.joinToString(",")}\n")
    }

    private fun extractModes(parameters: List<String>): Int =
        parameters.reversed().fold(0) { acc, s ->
            acc * 10 + when {
                s.isIndirectParameter() -> 0
                else -> 1
            }
        }

    private fun evaluate(literal: String): Long {
        if (literal.isWrappedIn('\'', '\''))
            return literal.unwrap()[0].toLong()
        val value = literal.toLongOrNull() ?: markers[literal]?.toLong()
        if (value == null) {
            unresolvedMarkers.getOrPut(literal) { mutableListOf() }.add(result.size)
        }
        return value ?: 0
    }

    private fun processAndStripMarker(line: String): String {
        if (line.contains(':')) {
            val (marker, rest) = line.split(Regex("""\s*:\s*"""), limit = 2)
            require(marker.isNotEmpty() && marker[0] !in listOf('@', '_')) {
                "User defined markers cannot start with @ or _"
            }
            require(!markers.containsKey(marker)) { "Duplicate definition of $marker" }
            markers[marker] = result.size
            //println("Marker $marker set to ${markers[marker]}")
            return rest
        }
        return line
    }

    private fun String.isIndirectParameter() =
        isWrappedIn('[', ']') && (this[1] != '+' && this[1] != '-')

    private fun String.isRelativeParameter() =
        isWrappedIn('[', ']') && (this[1] == '+' || this[1] == '-')


    private fun String.isWrappedIn(open: Char, close: Char) =
        startsWith(open) && endsWith(close)

    private fun String.unwrap() = slice(1 until length - 1)

}

fun String.compile() = SCAssembler(this).intProgram

fun main() {
    val intprog = SCAssembler(counter).intProgram
    SCDebugger(intprog).debug()
}