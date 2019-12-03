class Day01 : Day<Int>(1, 2019, ::asInts) {

    private val moduleMasses = input

    private fun fuelForMass(mass: Int) =
        (mass / 3 - 2).coerceAtLeast(0)

    private fun fuelForMassRecursive(mass: Int): Int {
        val fuel = fuelForMass(mass)
        return if (fuel == 0) 0 else fuel + fuelForMassRecursive(fuel)
    }

    override fun part1() = moduleMasses.sumBy { fuelForMass(it) }
    override fun part2() = moduleMasses.sumBy { fuelForMassRecursive(it) }

}

fun main() {
    Day01().run()
}