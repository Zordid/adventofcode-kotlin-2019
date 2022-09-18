import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day20Test {

    private val day20 = Day20()

    @Test
    fun `part 1`() {
        assertEquals(642, day20.part1())
    }

    @Test
    fun `part 2`() {
        assertEquals(7492, day20.part2())
    }

}