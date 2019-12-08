import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

typealias InstructionFunc = suspend (ip: Int, parameters: List<Int>) -> Int?

interface Debugger {
    fun preFetchHook(ip: Int)
    fun preParameterLoadHook(ip: Int, opcode: Int)
    fun preExecutionHook(ip: Int, instruction: Instruction, modes: String, parameters: List<Int>)
    fun postExecutionHook(ip: Int)
}

class Instruction(val opcode: Int, val name: String, val argDefinition: String, val operation: InstructionFunc) {
    val parameters: Int
        get() = argDefinition.length
}

class ShipComputer(
    private val program: List<Int> = emptyList(),
    private val input: suspend () -> Int = { print("INPUT: "); readLine()!!.toInt() },
    private val output: suspend (Int) -> Unit = { println("OUTPUT: $it") }
) {

    internal val instructionSet = listOf(
        Instruction(1, "ADD", "OOA") { ip, (op1, op2, addr) -> memory[addr] = op1 + op2; ip + 4 },
        Instruction(2, "MUL", "OOA") { ip, (op1, op2, addr) -> memory[addr] = op1 * op2; ip + 4 },
        Instruction(3, "IN", "A") { ip, (addr) -> memory[addr] = input(); ip + 2 },
        Instruction(4, "OUT", "O") { ip, (op) -> output(op); ip + 2 },
        Instruction(5, "JNZ", "OO") { ip, (op, dest) -> if (op != 0) dest else ip + 3 },
        Instruction(6, "JZ", "OO") { ip, (op, dest) -> if (op == 0) dest else ip + 3 },
        Instruction(7, "LT", "OOA") { ip, (op1, op2, addr) -> memory[addr] = if (op1 < op2) 1 else 0; ip + 4 },
        Instruction(8, "EQ", "OOA") { ip, (op1, op2, addr) -> memory[addr] = if (op1 == op2) 1 else 0; ip + 4 },
        Instruction(99, "HLT", "") { _, _ -> halt = true; null }
    )

    var debugger: Debugger? = null

    var memory = mutableListOf<Int>()
    var ip = 0
    var halt = false

    init {
        reset()
    }

    fun reset(initializer: MutableList<Int>.() -> Unit = {}) {
        memory = program.toMutableList()
        memory.initializer()
        ip = 0
        halt = false
    }

    private fun decodeInstruction(opcode: Int) =
        instructionSet.find { it.opcode == opcode % 100 }

    private fun Instruction.decodeModes(opcode: Int) =
        (opcode / 100).toString().reversed().padEnd(parameters, '0')

    private fun Instruction.fetchParameters(address: Int): List<Int> =
        argDefinition.indices.map { memory[address + it] }

    private fun Instruction.resolvePositionalArguments(modes: String, parameters: List<Int>): List<Int> =
        parameters.mapIndexed { n, p -> if (modes[n] == '0' && argDefinition[n] != 'A') memory[p] else p }

    suspend fun step(): Boolean {
        if (halt) return false
        debugger?.preFetchHook(ip)
        val opcode = memory[ip]
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

suspend fun List<Int>.execute(
    input: suspend () -> Int = { print("Your input: "); readLine()!!.toInt() },
    output: suspend (Int) -> Unit = { println(it) }
) =
    ShipComputer(this, input, output).runAsync()