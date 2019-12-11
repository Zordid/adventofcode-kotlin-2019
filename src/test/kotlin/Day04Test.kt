import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day04Test {

    @Test
    fun testPart1() {
        assertEquals(1048, Day04().part1())
    }

    @Test
    fun testPart2() {
        assertEquals(677, Day04().part2())
    }

    @Test
    fun testPart1Optimized() {
        assertEquals(1048, Day04().part1Optimized())
    }

    @Test
    fun testPart2Optimized() {
        assertEquals(677, Day04().part2Optimized())
    }

    @Test
    fun testPart1VeryOptimized() {
        assertEquals(1048, Day04().part1VeryOptimized())
    }

    @Test
    fun testPart2VeryOptimized() {
        assertEquals(677, Day04().part2VeryOptimized())
    }

}