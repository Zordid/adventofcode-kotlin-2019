class Day05 : Day<String>(5, 2019, ::asStrings) {

    private val program = input.justLongs()

    override fun part1(): Long {
        var result = 0L
        val c = ShipComputer(program, { 1 }, { result = it })
        c.run()
        return result
    }

    override fun part2(): Long {
        var result = 0L
        val c = ShipComputer(program, { 5 }, { result = it })
        c.run()
        return result
    }
}

fun main() {
    Day05().run()
}