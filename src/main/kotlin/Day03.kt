class Day03 : Day<String>(3, 2019, ::asStrings) {

}

fun main() {
    with(Day03()) {
        println(part1())
        println(part2())
    }
}