import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.math.max

fun String.toData() = toLong()

typealias InstructionFunc = suspend (ip: Long, parameters: List<Long>) -> Long?

interface Debugger {
    fun preFetchHook(ip: Long)
    fun preParameterLoadHook(ip: Long, opcode: Long)
    fun preExecutionHook(ip: Long, instruction: Instruction, modes: String, parameters: List<Long>)
    fun postExecutionHook(ip: Long)
}

class Instruction(val opcode: Long, val name: String, val argDefinition: String, val operation: InstructionFunc) {
    val parameters: Int
        get() = argDefinition.length
}

suspend fun defaultConsoleInput(): Long {
    print("INPUT: ")
    return readLine()!!.toData()
}

suspend fun defaultConsoleOutput(v: Long) {
    println("OUTPUT: $v")
}

interface Memory<T> {
    val size: Int
    val hwm: Int
    operator fun get(address: T): T
    operator fun set(address: T, value: T)
    fun set(address: T, values: Collection<T>)
    fun load(program: Collection<Long>)
}

class LongMemory : Memory<Long> {
    private var ram = LongArray(100)
    private var maxAddress = 0

    override val size: Int
        get() = ram.size

    override val hwm: Int
        get() = maxAddress

    override fun get(address: Long) =
        if (address.toInt() >= ram.size) 0 else ram[address.toInt()]

    override fun set(address: Long, value: Long) {
        ensureAddress(address.toInt())
        ram[address.toInt()] = value
    }

    override fun set(address: Long, values: Collection<Long>) {
        ensureAddress(address.toInt() + values.size - 1)
        values.toLongArray().copyInto(ram, address.toInt(), 0)
    }

    override fun load(program: Collection<Long>) {
        ram = program.toLongArray()
    }

    private fun ensureAddress(address: Int) {
        require(address in (0L..Int.MAX_VALUE))
        maxAddress = max(address, hwm)
        if (address >= ram.size) {
            val newSize = ((address / 1000) + 1) * 1000
            ram = LongArray(newSize).apply {
                ram.copyInto(this, 0, 0)
            }
        }
    }

}

fun byValues(vararg values: Long): suspend () -> Long =
    values.iterator().let {
        { it.nextLong() }
    }

fun byCapturing(outputs: MutableList<Long>): suspend (Long) -> Unit = { outputs.add(it) }
fun byInChannel(channel: Channel<Long>): suspend () -> Long = { channel.receive() }
fun byOutChannel(channel: Channel<Long>): suspend (Long) -> Unit = { channel.send(it) }

fun byAsciiOutChannel(
    channel: Channel<String>,
    buffer: StringBuilder = StringBuilder()
): suspend (Long) -> Unit = { v: Long ->
    when (v) {
        10L -> channel.send(buffer.toString()).also { buffer.clear() }
        in 11..126 -> buffer.append(v.toChar())
        else -> channel.send("$v")
    }
}



class IntcodeComputer(
    private val program: List<Long> = emptyList(),
    private val input: suspend () -> Long = ::defaultConsoleInput,
    private val output: suspend (Long) -> Unit = ::defaultConsoleOutput
) {

    internal val instructionSet = listOf(
        Instruction(1, "ADD", "OOA") { ip, (op1, op2, addr) -> writeMem(addr, op1 + op2); ip + 4 },
        Instruction(2, "MUL", "OOA") { ip, (op1, op2, addr) -> writeMem(addr, op1 * op2); ip + 4 },
        Instruction(3, "IN", "A") { ip, (addr) -> writeMem(addr, input()); ip + 2 },
        Instruction(4, "OUT", "O") { ip, (op) -> output(op); ip + 2 },
        Instruction(5, "JNZ", "OO") { ip, (op, dest) -> if (op != 0L) dest else ip + 3 },
        Instruction(6, "JZ", "OO") { ip, (op, dest) -> if (op == 0L) dest else ip + 3 },
        Instruction(7, "LT", "OOA") { ip, (op1, op2, addr) -> writeMem(addr, if (op1 < op2) 1 else 0); ip + 4 },
        Instruction(8, "EQ", "OOA") { ip, (op1, op2, addr) -> writeMem(addr, if (op1 == op2) 1 else 0); ip + 4 },
        Instruction(9, "SRB", "O") { ip, (op) -> rb += op; ip + 2 },
        Instruction(99, "HLT", "") { _, _ -> halt = true; null }
    )

    var debugger: Debugger? = null

    var memory: Memory<Long> = LongMemory()
    var ip = 0L
    var rb = 0L
    var halt = false

    init {
        reset()
    }

    fun reset(initializer: Memory<Long>.() -> Unit = {}) {
        memory.load(program.toMutableList())
        memory.initializer()
        ip = 0
        rb = 0
        halt = false
    }

    private fun decodeInstruction(opcode: Long) =
        instructionSet.find { it.opcode == opcode % 100 }

    private fun Instruction.decodeModes(opcode: Long) =
        (opcode / 100).toString().reversed().padEnd(parameters, '0')

    private fun Instruction.fetchParameters(address: Long): List<Long> =
        argDefinition.indices.map { readMem(address + it) }

    private fun Instruction.resolvePositionalArguments(modes: String, parameters: List<Long>): List<Long> =
        parameters.mapIndexed { n, p ->
            val forResult = argDefinition[n] == 'A'
            when (modes[n]) {
                '0' -> if (forResult) p else readMem(p)
                '1' -> if (forResult) error("can't process immediate mode for result") else p
                '2' -> if (forResult) rb + p else readMem(rb + p)
                else -> error("wrong mode $modes[n]")
            }
        }

    private fun readMem(address: Long) = memory[address]

    private fun writeMem(address: Long, value: Long) {
        memory[address] = value
    }

    suspend fun step(): Boolean {
        if (halt) return false
        debugger?.preFetchHook(ip)
        val opcode = readMem(ip)
        val instruction = decodeInstruction(opcode) ?: error("Unknown instruction $opcode at $ip")
        debugger?.preParameterLoadHook(ip, opcode)
        val parameters = instruction.fetchParameters(ip + 1)
        val modes = instruction.decodeModes(opcode)
        debugger?.preExecutionHook(ip, instruction, modes, parameters)
        val args = instruction.resolvePositionalArguments(modes, parameters)
        ip = instruction.operation(ip, args) ?: -1
        debugger?.postExecutionHook(ip)
        return true
    }

    suspend fun runAsync() {
        while (!halt) {
            step()
            yield()
        }
    }

    fun run() {
        runBlocking { runAsync() }
    }

}

fun List<Long>.execute(
    input: suspend () -> Long = ::defaultConsoleInput,
    output: suspend (Long) -> Unit = ::defaultConsoleOutput
) =
    IntcodeComputer(this, input, output).run()

typealias IntcodeProgram = List<Long>

fun List<String>.asIntcode() = first().split(",").map { it.toLong() }