typealias Instruction = (Int, List<Int>) -> Int

class ShipComputer(
    private val program: List<Int>,
    private val defaultInitializer: MutableList<Int>.() -> Unit = {}
) {

    lateinit var memory: MutableList<Int>

    var halt = false
    var ip = 0
    private val instructions = mapOf<Int, Instruction>(
        1 to { ip, args -> memory[args[2]] = memory[args[0]] + memory[args[1]]; ip + 4 },
        2 to { ip, args -> memory[args[2]] = memory[args[0]] * memory[args[1]]; ip + 4 },
        99 to { _, _ -> halt = true; 0 }
    )

    init {
        reset()
    }

    fun reset(initializer: MutableList<Int>.() -> Unit = {}) {
        memory = program.toMutableList()
        memory.defaultInitializer()
        memory.initializer()
        halt = false
        ip = 0
    }

    fun run() {
        while (!halt) {
            val opcode = memory[ip]
            val args = memory.slice(ip + 1..ip + 3)
            val instruction = instructions[opcode] ?: error("Unknown opcode $opcode!")
            ip = instruction(ip, args)
        }
    }

}

class Day02 : Day<String>(2, 2019, ::asStrings) {

    private val program = input[0].split(",").map { it.toInt() }

    override fun part1(): Int {
        val computer = ShipComputer(program) {
            set(1, 12, 2)
        }

        computer.run()
        return computer.memory[0]
    }

    override fun part2(): Int {
        val computer = ShipComputer(program)
        return (0..9999).first { idx ->
            val noun = idx / 100
            val verb = idx % 100
            computer.reset {
                set(1, noun, verb)
            }
            computer.run()
            computer.memory[0] == 19690720
        }
    }

}

fun <T> MutableList<T>.set(index: Int, vararg elements: T) =
    elements.forEachIndexed { offset, t -> set(index + offset, t) }

fun main() {
    Day02().run()
}
