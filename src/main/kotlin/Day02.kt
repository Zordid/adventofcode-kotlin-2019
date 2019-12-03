typealias Instruction = (Int, List<Int>) -> Int

class ShipComputer(
    private val program: List<Int>,
    private val initializer: MutableList<Int>.() -> Unit = {}
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

    fun reset() {
        memory = program.toMutableList()
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
            this[1] = 12
            this[2] = 2
        }

        computer.run()
        return computer.memory[0]
    }

    override fun part2(): Int {
        for (noun in 0..99) {
            for (verb in 0..99) {
                val computer = ShipComputer(program) {
                    this[1] = noun
                    this[2] = verb
                }
                computer.run()
                if (computer.memory[0] == 19690720) {
                    return 100 * noun + verb
                }
            }
        }
        return -1
    }

}

fun main() {
    Day02().run()
}