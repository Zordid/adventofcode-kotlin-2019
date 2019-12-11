import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

typealias Data = Long

fun String.toData() = toLong()

typealias InstructionFunc = suspend (ip: Data, parameters: List<Data>) -> Data?

interface Debugger {
    fun preFetchHook(ip: Data)
    fun preParameterLoadHook(ip: Data, opcode: Data)
    fun preExecutionHook(ip: Data, instruction: Instruction, modes: String, parameters: List<Data>)
    fun postExecutionHook(ip: Data)
}

class Instruction(val opcode: Data, val name: String, val argDefinition: String, val operation: InstructionFunc) {
    val parameters: Int
        get() = argDefinition.length
}

suspend fun defaultConsoleInput(): Data {
    print("INPUT: ")
    return readLine()!!.toData()
}

suspend fun defaultConsoleOutput(v: Data) {
    println("OUTPUT: $v")
}

class ShipComputer(
    private val program: List<Data> = emptyList(),
    private val input: suspend () -> Data = ::defaultConsoleInput,
    private val output: suspend (Data) -> Unit = ::defaultConsoleOutput
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

    var memory = mutableListOf<Data>()
    var ip = 0L
    var rb = 0L
    var halt = false

    init {
        reset()
    }

    fun reset(initializer: MutableList<Data>.() -> Unit = {}) {
        memory = program.toMutableList()
        memory.initializer()
        ip = 0
        rb = 0
        halt = false
    }

    private fun decodeInstruction(opcode: Data) =
        instructionSet.find { it.opcode == opcode % 100 }

    private fun Instruction.decodeModes(opcode: Data) =
        (opcode / 100).toString().reversed().padEnd(parameters, '0')

    private fun Instruction.fetchParameters(address: Data): List<Data> =
        argDefinition.indices.map { readMem(address + it) }

    private fun Instruction.resolvePositionalArguments(modes: String, parameters: List<Data>): List<Data> =
        parameters.mapIndexed { n, p ->
            when (modes[n]) {
                '0' -> if (argDefinition[n] == 'A') p else readMem(p)
                '1' -> p
                '2' -> if (argDefinition[n] == 'A') rb + p else readMem(rb + p)
                else -> error("wrong mode $modes[n]")
            }
        }

    fun readMem(address: Data): Data {
        require(address >= 0)
        while (address >= memory.size) memory.add(0)
        return memory[address.toInt()]
    }

    fun writeMem(address: Data, value: Data) {
        require(address >= 0)
        while (address >= memory.size) memory.add(0)
        memory[address.toInt()] = value
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

fun List<Data>.execute(
    input: suspend () -> Data = ::defaultConsoleInput,
    output: suspend (Data) -> Unit = ::defaultConsoleOutput
) =
    ShipComputer(this, input, output).run()