import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.atan2

internal class Day10Test {

    @Nested
    inner class Final {
        @Test
        fun `part 1`() {
            assertEquals(334, Day10().part1())
        }

        @Test
        fun `part 2`() {
            assertEquals(1119, Day10().part2())
        }
    }

    fun toAngle(v: Point): Double {
        val v2 = v.y to -v.x
        return atan2(v2.y.toDouble(), v2.x.toDouble())
    }

    @Test
    fun again() {
        val field = """
            #..#...
            ....#..
            ....#.#
            ...#...
            ..#....
            ...#...
            ...#..#
        """.trimIndent().split("\n")

        val d = Day10(field)
        val v = d.field.visibleFromByAngle(3 to 3)
        v.entries.sortedBy { toAngle(it.key) }.forEach {
            println(
                "${it.key} => ${
                    atan2(
                        it.key.y.toDouble(),
                        it.key.x.toDouble()
                    )
                } / ${toAngle(it.key)} = ${it.value}"
            )
        }
    }

    @Test
    fun `part 1 demo with small field`() {
        val field = """
            .#..#
            .....
            #####
            ....#
            ...##
        """.trimIndent().split("\n")

        val expected = """
            .7..7
            .....
            67775
            ....7
            ...87
        """.trimIndent()

        val day10 = Day10(field)
        val asteroids = day10.field.asteroids
        val result =
            allPointsInArea(
                origin,
                4 to 4
            ).map { a -> if (a in asteroids) day10.field.countVisibleFrom(a).toString() else "." }
                .chunked(5).joinToString("\n") { it.joinToString("") }

        assertEquals(expected, result)
    }

    @Test
    fun `part 2 demo with big field`() {
        val field = """
            .#..##.###...#######
            ##.############..##.
            .#.######.########.#
            .###.#######.####.#.
            #####.##.#.##.###.##
            ..#####..#.#########
            ####################
            #.####....###.#.#.##
            ##.#################
            #####.##.###..####..
            ..######..##.#######
            ####.##.####...##..#
            .#####..#.######.###
            ##...#.##########...
            #.##########.#######
            .####.#.###.###.#.##
            ....##.##.###..#####
            .#.#.###########.###
            #.#.#.#####.####.###
            ###.##.####.##.#..##
        """.trimIndent().split("\n")

        val day10 = Day10(field)
        val hits = listOf(0 to 0) + day10.field.hitSequenceFrom(day10.bestStation()).take(300).toList()

        assertEquals(11 to 12, hits[1])
        assertEquals(12 to 1, hits[2])
        assertEquals(12 to 2, hits[3])
        assertEquals(12 to 8, hits[10])
        assertEquals(16 to 0, hits[20])
        assertEquals(16 to 9, hits[50])
        assertEquals(10 to 16, hits[100])
        assertEquals(9 to 6, hits[199])
        assertEquals(8 to 2, hits[200])
        assertEquals(10 to 9, hits[201])
        assertEquals(11 to 1, hits[299])
    }

}