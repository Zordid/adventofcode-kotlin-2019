import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class Day13Test {

    @Test
    fun `part 1`() {
        assertEquals(304, Day13().part1())
    }

    @Test
    fun `part 2`() {
        assertEquals(14747, Day13().part2())
    }

}