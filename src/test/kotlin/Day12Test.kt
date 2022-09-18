import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12Test {

    @Test
    fun `part 1`() {
        assertEquals(8625, Day12().part1())
    }

    @Test
    fun `part 2`() {
        assertEquals(332477126821644L, Day12().part2())
    }

}