import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day09Test {

    @Test
    fun `part 1`() {
        assertEquals(3512778005L, Day09().part1())
    }

    @Test
    fun `part 2`() {
        assertEquals(35920L, Day09().part2())
    }

}