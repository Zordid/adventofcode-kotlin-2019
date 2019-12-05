class Day02 : Day<String>(2, 2019, ::asStrings) {

    private val program = input[0].split(",").map { it.toInt() }

    override fun part1(): Int {
        val computer = ShipComputer(program)
        computer.reset {
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
