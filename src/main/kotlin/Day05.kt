class Day05 : Day<String>(5, 2019, ::asStrings) {

    private val program = input.justInts()

    override fun part1(): Int {
        var result = 0
        val c = ShipComputer(program, { 1 }, { result = it })
        c.run()
        return result
    }

    override fun part2(): Int {
        var result = 0
        val c = ShipComputer(program, { 5 }, { result = it })
        c.run()
        return result
    }
}

fun main() {
    Day05().run()
}