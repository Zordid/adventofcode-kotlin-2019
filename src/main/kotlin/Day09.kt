class Day09(testData: List<String>? = null) : Day<String>(9, 2019, ::asStrings, testData) {

    override fun part1(): Any? {
        input.justLongs().execute()
        return super.part1()
    }

    override fun part2(): Any? {
        return super.part2()
    }
}

fun main() {
    Day09().run()
}