typealias InstructionFunc = (Int, List<Int>) -> Int?

class Instruction(val opcode: Int, val name: String, val argDefinition: String, val operation: InstructionFunc)

class ShipComputer(
    private val program: List<Int>,
    private val input: () -> Int = { print("INPUT: "); readLine()!!.toInt() },
    private val output: (Int) -> Unit = { println("OUTPUT: $it") }
) {

    val instructionSet = listOf(
        Instruction(1, "ADD", "OOA") { ip, (op1, op2, addr) -> memory[addr] = op1 + op2; ip + 4 },
        Instruction(2, "MUL", "OOA") { ip, (op1, op2, addr) -> memory[addr] = op1 * op2; ip + 4 },
        Instruction(3, "IN ", "A") { ip, (addr) -> memory[addr] = input(); ip + 2 },
        Instruction(4, "OUT", "O") { ip, (op) -> output(op); ip + 2 },
        Instruction(5, "JNZ", "OO") { ip, (op, dest) -> if (op != 0) dest else ip + 3 },
        Instruction(6, "JZ ", "OO") { ip, (op, dest) -> if (op == 0) dest else ip + 3 },
        Instruction(7, "LT ", "OOA") { ip, (op1, op2, addr) -> memory[addr] = if (op1 < op2) 1 else 0; ip + 4 },
        Instruction(8, "EQ ", "OOA") { ip, (op1, op2, addr) -> memory[addr] = if (op1 == op2) 1 else 0; ip + 4 },
        Instruction(99, "HLT", "") { _, _ -> null }
    )

    private fun processArgs(modes: Int, argDefinition: String, startAddr: Int): List<Int> {
        var mode = modes
        var addr = startAddr
        return argDefinition.map { a ->
            val arg = memory[addr++]
            when {
                a == 'O' && mode % 10 == 0 -> memory[arg]
                else -> arg
            }.also { mode /= 10 }
        }
    }

    var debug = false
    var memory = mutableListOf<Int>()
    var ip = 0
    var cycles = 0

    init {
        reset()
    }

    fun reset(initializer: MutableList<Int>.() -> Unit = {}) {
        memory = program.toMutableList()
        memory.initializer()
        ip = 0
    }

    fun run() {
        while (true) {
            val opcode = memory[ip] % 100
            val modes = memory[ip] / 100
            val instruction = instructionSet.find { it.opcode == opcode } ?: error("Unknown instruction $opcode at $ip")
            val args = processArgs(modes, instruction.argDefinition, ip + 1)
            try {
                if (debug) println("$ip ${instruction.name} $args")
                ip = instruction.operation(ip, args) ?: break
            } catch (e: Exception) {
                error("$ip OP:$opcode MODES:$modes ARGS:$args - ${e.message}")
            }
            cycles++
        }
    }

}