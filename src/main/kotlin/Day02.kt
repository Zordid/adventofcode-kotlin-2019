class Day02 : Day<String>(2, 2019, ::asStrings) {

    private val program = input.justLongs()

    override fun part1(): Long {
        val computer = IntcodeComputer(program)
        computer.reset {
            set(1, listOf<Long>(12, 2))
        }

        computer.run()
        return computer.memory[0]
    }

    override fun part2(): Long {
        val computer = IntcodeComputer(program)
        return (0..9999L).first { idx ->
            val noun = idx / 100
            val verb = idx % 100
            computer.reset {
                set(1, listOf(noun, verb))
            }
            computer.run()
            computer.memory[0] == 19690720L
        }
    }

}

fun <T> MutableList<T>.set(index: Int, vararg elements: T) =
    elements.forEachIndexed { offset, t -> set(index + offset, t) }

fun main() {
    Day02().run()
}
