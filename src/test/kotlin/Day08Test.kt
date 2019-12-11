import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day08Test {

    @Test
    fun testPart1() {
        assertEquals(2250, Day08().part1())
    }

    @Test
    fun testPart2() {
        val expectedMessage = """
            #### #  #   ## #  # #
            #    #  #    # #  # #
            ###  ####    # #  # #
            #    #  #    # #  # #
            #    #  # #  # #  # #
            #    #  #  ##   ##  ####
        """.trimIndent()
        assertEquals(expectedMessage, Day08().part2())
    }

    @Test
    fun testMinimalExamplePart2() {
        val input = listOf("0222112222120000")
        val expectedPicture = """
             #
            #
        """.trimIndent()
        assertEquals(expectedPicture, Day08(input, 2, 2).part2())
    }

}