import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day11Test {

    @Test
    fun `part 1`() {
        assertEquals(1564, Day11().part1())
    }

    @Test
    fun `part 2`() {
        val expected = """
            ###  #### #### ###   ##  #### #### ### 
            #  # #    #    #  # #  # #    #    #  #
            #  # ###  ###  #  # #    ###  ###  ### 
            ###  #    #    ###  #    #    #    #  #
            # #  #    #    #    #  # #    #    #  #
            #  # #    #### #     ##  #    #### ### 
        """.trimIndent()
        assertEquals(expected, Day11().part2())
    }
}