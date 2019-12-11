import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day06Test {

    @Test
    fun testPart1() {
        assertEquals(245089, Day06().part1())
    }

    @Test
    fun testPart2() {
        assertEquals(511, Day06().part2())
    }

    @Test
    fun testPart1Example() {
        val test = """
            COM)B
            B)C
            C)D
            D)E
            E)F
            B)G
            G)H
            D)I
            E)J
            J)K
            K)L
        """.trimIndent().split("\n")
        assertEquals(42, Day06(test).part1())
    }

    @Test
    fun testPart2Example() {
        val test = """
            COM)B
            B)C
            C)D
            D)E
            E)F
            B)G
            G)H
            D)I
            E)J
            J)K
            K)L
            K)YOU
            I)SAN
        """.trimIndent().split("\n")
        assertEquals(4, Day06(test).part2())
    }
}