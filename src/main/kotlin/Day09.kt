class Day09(testData: List<String>? = null) : Day<String>(9, 2019, ::asStrings, testData) {

    private val program = input.justLongs()

    override fun part1(): Long {
        var result = 0L
        program.execute(input = { 1 }, output = { result = it })
        return result
    }

    override fun part2(): Long {
        var result = 0L
        program.execute(input = { 2 }, output = { result = it })
        return result
    }
}

fun main() {
    Day09().run()
}