import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day19Test {

    @Test
    fun `part 1`() {
        assertEquals(206, Day19().part1())
    }

    @Test
    fun `part 2`() {
        assertEquals(6190948, Day19().part2())
    }
}