package shipcomputer

import Debugger
import Instruction
import IntcodeComputer
import Memory
import defaultConsoleInput
import defaultConsoleOutput
import getInputAsLongs
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import util.PixelGameEngine
import x
import y
import java.awt.Color
import java.util.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

val program1 = listOf(3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8)

class SCDebugger(
    val program: List<Long>,
    input: suspend () -> Long = ::defaultConsoleInput,
    private val output: (suspend ((Long)) -> Unit)? = null
) : Debugger, PixelGameEngine() {

    val computer = IntcodeComputer(program, output = ::output, input = input)
    var detailedLog = true
    val memory = InterceptedMemory(computer.memory)

    val halt: Boolean
        get() = computer.halt

    val recordedOutput = mutableListOf<Long>()

    init {
        computer.debugger = this
        computer.memory = memory
    }

    suspend fun output(v: (Long)) {
        recordedOutput.add(v)
        output?.invoke(v)
    }

    suspend fun runAsync() {
        while (!halt) {
            computer.step()
            yield()
        }
    }

    fun debug(maxCycles: Int = Int.MAX_VALUE) {
        try {
            while (!computer.halt && cycle < maxCycles) runBlocking { computer.step() }
        } catch (e: Exception) {
            println("CRASH DETECTED: ${e.message}")
        }
        println()

        dump()
    }

    enum class Type { OPCODE, PARAMETER, DATA }

    class InterceptedMemory(val memory: Memory<Long>) : Memory<Long> by memory {

        var currentMode = Type.OPCODE
        val writeCount = mutableMapOf<Int, Int>()
        val readCount = mutableMapOf<Int, Int>()
        val type = mutableMapOf<Int, MutableList<Type>>()

        override operator fun set(address: Long, value: (Long)) {
            println("Write $value to [$address]")
            memory[address] = value
            writeCount[address.toInt()] = (writeCount[address.toInt()] ?: 0) + 1
        }

        override operator fun get(address: Long): (Long) {
            println("Read ${memory[address]} from [$address] ($currentMode)")
            readCount[address.toInt()] = (readCount[address.toInt()] ?: 0) + 1
            if (type[address.toInt()]?.lastOrNull() != currentMode)
                type.getOrPut(address.toInt()) { mutableListOf() }.add(currentMode)
            return memory[address]
        }

        fun logMem(block: (Int) -> String) {
            var address = 0
            while (address < memory.hwm) {
                print("%4d ".format(address))
                repeat(10) {
                    if (address < memory.hwm) {
                        print(block(address))
                        print("  ")
                        address += 10
                    }
                }
                println()
            }
        }

        fun logRWBlock(address: Int) =
            (address until min(address + 10, memory.size)).joinToString("") {
                when {
                    (writeCount[it] ?: 0) > 0 && (readCount[it] ?: 0) > 0 -> "w"
                    (writeCount[it] ?: 0) > 0 -> "!"
                    (readCount[it] ?: 0) > 0 -> "r"
                    else -> "."
                }
            }

        fun logUsageBlock(address: Int) =
            (address until min(address + 10, memory.hwm)).joinToString("") {
                val types = type[it] ?: emptyList()
                val type = when {
                    types.size == 1 && types.first() == Type.OPCODE -> "o"
                    types.size == 1 && types.first() == Type.PARAMETER -> "p"
                    types.size == 1 && types.first() == Type.DATA -> "d"
                    types.size > 1 -> "M"
                    else -> "."
                }
                if ((writeCount[it] ?: 0) > 0) type.uppercase(Locale.getDefault()) else type
            }

        fun dump() {
            println("\nUSAGE ANALYSIS")
            logMem(::logUsageBlock)
            println("\nR/W ANALYSIS")
            logMem(::logRWBlock)
            println("\nMEMORY WEAR")
            println(writeCount.entries.asSequence().sortedByDescending { it.value }
                .take(10).takeWhile { (_, count) -> count > 0 }
                .joinToString("\n") { (address, count) -> "[$address] x $count" })
        }

    }

    data class Execution(val instruction: Instruction, val modes: String, val parameters: List<Long>) {
        var count: Int = 1

        override fun toString() =
            "${instruction.name.padEnd(3, ' ')} ${
                parameters.mapIndexed { index, p ->
                    val prefix = if (instruction.argDefinition[index] == 'A')
                        "->" else ""
                    val destAndMode = when (modes[index]) {
                        '0' -> "[$p]"
                        '1' -> "$p"
                        '2' -> if (p >= 0) "[RB+$p]" else "[RB$p]"
                        else -> "?$p?"
                    }
                    prefix + destAndMode
                }.joinToString(" ")
            }"
    }

    val previousIp = mutableMapOf<(Long), MutableMap<(Long), (Long)>>()
    val nextIp = mutableMapOf<(Long), MutableMap<(Long), (Long)>>()
    val linearExecutionLog = mutableListOf<Pair<(Long), Execution>>()
    val executionHistory = mutableMapOf<(Long), MutableList<Execution>>()
    var cycle = 0
    var lastIp = -1L

    override fun preFetchHook(ip: (Long)) {
        memory.currentMode = Type.OPCODE
        previousIp.getOrPut(ip) { mutableMapOf() }.getOrPut(lastIp) { 0 }
        previousIp[ip]!![lastIp] = previousIp[ip]!![lastIp]!! + 1
        lastIp = ip
    }

    override fun preParameterLoadHook(ip: (Long), opcode: (Long)) {
        memory.currentMode = Type.PARAMETER
    }

    override fun preExecutionHook(ip: (Long), instruction: Instruction, modes: String, parameters: List<Long>) {
        if (detailedLog) println("${"%04d ".format(ip)} ${instruction.name} $parameters")
        memory.currentMode = Type.DATA

        val exe = Execution(instruction, modes, parameters)
        linearExecutionLog.add(ip to exe)
        val hist = executionHistory.getOrPut(ip) { mutableListOf() }
        if (hist.lastOrNull() == exe)
            hist.last().count++
        else
            hist.add(exe)
    }

    override fun postExecutionHook(ip: (Long)) {
        cycle++
        nextIp.getOrPut(lastIp) { mutableMapOf() }.getOrPut(ip) { 0 }
        nextIp[lastIp]!![ip] = nextIp[lastIp]!![ip]!! + 1
    }

    fun dump() {
        println("\nANALYSIS")
        println("Program size: ${program.size}")
        println("Memory usage: ${memory.hwm}")

        memory.dump()
        println("\nPROGRAM")
        val orderedIps = executionHistory.keys.sorted()

        orderedIps.forEachIndexed { idx, ip ->
            val previous = if (idx > 0) orderedIps[idx - 1] else -1
            val nextToPrint = if (idx < orderedIps.size - 1) orderedIps[idx + 1] else -1
            val origins = previousIp[ip]!!
            val destinations = nextIp[ip] ?: emptyMap<Int, Int>()
            if (origins.size > 1 || (origins[previous] ?: 0L) == 0L) {
                if (!origins.containsKey(previous))
                    println("\n===[${jumpstats(origins)}]===")
                else
                    println("\n---[${jumpstats(origins)}]---")
            }
            val exes = executionHistory[ip]!!

            println("%04d %s".format(ip, exes.report()))
            if (destinations.size > 1 || (destinations[nextToPrint] ?: 0) == 0) {
                if (!destinations.containsKey(nextToPrint))
                    println("===")
                else
                    println()
            }
        }
        println("A total of ${executionHistory.size} instructions were called in $cycle cycles.")

        println("\nRECORDED OUTPUT (${recordedOutput.size})")
        println(recordedOutput)
    }

    private fun jumpstats(origins: Map<(Long), (Long)>) =
        origins.toSortedMap().asSequence().joinToString { "${"%04d".format(it.key)} (x${it.value})" }

    fun List<Execution>.report(): String {
        if (size > 1 && all { it.instruction == first().instruction && it.modes == first().modes }) {
            // all identical except parameters
            val detectChanging = first().parameters.indices.filter { pIdx ->
                !all { it.parameters[pIdx] == first().parameters[pIdx] }
            }
            val changingReport = joinToString { exe ->
                detectChanging.map { exe.parameters[it] }.toString()
            }
            return "%4sx %-25s with varying %s: %s".format(
                sumOf { it.count },
                first().toString(),
                detectChanging.joinToString { "p${it + 1}" },
                changingReport
            )
        }
        return joinToString(" -> ") {
            "%5s %-25s".format(if (it.count > 1) "${it.count}x" else "", it.toString())
        }
    }

    val cellDim = 5
    var dim = 0

    override fun onCreate() {
        dim = screenWidth / cellDim
    }

    var executedIp = 0L
    var executedInstruction: Instruction? = null

    fun markAddress(address: (Long), color: Color) {
        val c = address.toInt() % dim to address.toInt() / dim
        drawRect(c.x * cellDim, c.y * cellDim, cellDim, cellDim, color)
    }

    val Type.color: Color
        get() = when (this) {
            Type.DATA -> Color.CYAN
            Type.OPCODE -> Color.RED
            Type.PARAMETER -> Color.GREEN
        }

    override fun onUpdate(elapsedTime: Long) {
        if (computer.halt) {
            if (executedIp != 0L)
                dump()
            executedIp = 0
            sleep(1000)
            return
        }

        linearExecutionLog.lastOrNull()?.let { (ip, exe) ->
            (ip..ip + exe.instruction.parameters).forEach {
                markAddress(it, Color.DARK_GRAY)
            }
        }
        executedIp = computer.ip
        runBlocking { computer.step() }
        linearExecutionLog.lastOrNull()?.let { (ip, exe) ->
            (ip..ip + exe.instruction.parameters).forEach {
                markAddress(it, Color.RED)
            }
        }
        for (y in (0..dim)) {
            for (x in (0..dim)) {
                val address = y * dim + x
                if (address <= memory.hwm) {
                    val type = memory.type[address]?.lastOrNull()
                    val color = type?.color ?: Color.DARK_GRAY
                    draw(x * cellDim + 2, y * cellDim + 2, color)

                    if ((memory.writeCount[address] ?: 0) > 0)
                        drawRect(x * cellDim + 1, y * cellDim + 1, 3, 3, Color.ORANGE)
                }
            }
        }
        sleep(20)
    }

}

fun List<Long>.debug() = SCDebugger(this).debug()

fun List<Long>.debugGraphical(
    input: suspend () -> Long = ::defaultConsoleInput,
    output: suspend ((Long)) -> Unit = ::defaultConsoleOutput
) =
    with(SCDebugger(this, input, output)) {
        val width = (ceil(sqrt(memory.size.toDouble()))).roundToInt()
        require(width * width >= memory.size)
        construct(width * cellDim, width * cellDim, 8, 8)
        start()
    }

fun main() {
    val inputValues = listOf<Long>(5, 2, 3, 4, 7, 99, 1, 3, 4, 8, 6, 3, 1).asSequence().iterator()
    getInputAsLongs(7).debugGraphical({ inputValues.next() })
}