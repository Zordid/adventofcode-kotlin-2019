import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day05Test {

    private val day5 = Day05()

    @Test
    fun testPart1() {
        assertEquals(15508323, day5.part1())
    }

    @Test
    fun testPart2() {
        assertEquals(9006327, day5.part2())
    }

}