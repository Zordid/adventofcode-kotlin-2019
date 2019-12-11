import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day02Test {

    private val day2 = Day02()

    @Test
    fun testShipComputer() {
        val computer = ShipComputer(listOf(1, 9, 10, 3, 2, 3, 11, 0, 99, 30, 40, 50))
        computer.run()
        assertEquals(3500, computer.memory[0])
    }

    @Test
    fun testPart1() {
        assertEquals(3850704, day2.part1())
    }

    @Test
    fun testPart2() {
        assertEquals(6718, day2.part2())
    }


}