import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day01KtTest {

    private val day01 = Day01()

    @Test
    fun part1() {
        assertEquals(3465154, day01.part1())
    }

    @Test
    fun part2() {
        assertEquals(5194864, day01.part2())
    }

}