import util.AStarSearch
import util.Graph
import util.breadthFirstSearch
import util.buildStack
import kotlin.math.absoluteValue

class Day20(testData: List<String>? = null) : Day<String>(20, 2019, ::asStrings, testData) {

    val maze = input.map { it.toList() }
    private val area = origin to (maze.first().lastIndex to maze.lastIndex)
    private val portals: Map<String, Set<Point>>
    private val start: Point
    private val end: Point

    init {
        val p = mutableMapOf<String, MutableSet<Point>>()

        area.allPoints().forEach { candidate ->
            if (maze[candidate] == '.') {
                val directionOfLetter = Direction.values().singleOrNull { maze[candidate.neighbor(it)] in 'A'..'Z' }
                if (directionOfLetter != null) {
                    val firstChar = maze[candidate.neighbor(directionOfLetter, 1)]
                    val secondChar = maze[candidate.neighbor(directionOfLetter, 2)]
                    val portalName = if (directionOfLetter == Direction.DOWN || directionOfLetter == Direction.RIGHT)
                        "$firstChar$secondChar"
                    else
                        "$secondChar$firstChar"

                    p.getOrPut(portalName) { mutableSetOf() }.add(candidate)
                }
            }
        }
        require(p.count { it.value.size == 2 } == p.size - 2)
        require(p.count { it.value.size == 1 } == 2)

        start = p["AA"]!!.single()
        end = p["ZZ"]!!.single()
        p -= "AA"
        p -= "ZZ"
        portals = p
    }

    override fun part1(): Int {
        val connections = (portals.map { it.value.first() to it.value.last() } +
                portals.map { it.value.last() to it.value.first() }).toMap()
        val graph = object : Graph<Point> {
            override fun neighborsOf(node: Point) =
                node.neighbors().filter { maze[it] == '.' } +
                        (connections[node]?.let { listOf(it) } ?: emptyList())
        }

        val solution = graph.breadthFirstSearch(start, end)
        return solution.size - 1
    }

    data class RecursivePosition(val level: Int, val p: Point)

    override fun part2(): Int {
        val innerPortalToPoint = portals
            .map { entry -> entry.key to entry.value.single { it.isInnerPortal() } }.toMap()
        val outerPortalToPoint = portals
            .map { entry -> entry.key to entry.value.single { it.isOuterPortal() } }.toMap()

        val pointToInnerPortal = innerPortalToPoint.flip()
        val pointToOuterPortal = outerPortalToPoint.flip()

        val graph = object : Graph<RecursivePosition> {
            override fun neighborsOf(node: RecursivePosition): Collection<RecursivePosition> {
                val directNeighbors = node.p.neighbors()
                    .filter { p -> maze[p] == '.' }.map { node.copy(p = it) }

                val innerPortalName = pointToInnerPortal[node.p]
                val outerPortalName = pointToOuterPortal[node.p]
                val portalNeighbor =
                    when {
                        innerPortalName != null ->
                            RecursivePosition(node.level + 1, outerPortalToPoint[innerPortalName]!!)
                        outerPortalName != null && node.level > 0 ->
                            RecursivePosition(node.level - 1, innerPortalToPoint[outerPortalName]!!)
                        else -> null
                    }
                return if (portalNeighbor != null) directNeighbors + portalNeighbor else directNeighbors
            }

            override fun costEstimation(from: RecursivePosition, to: RecursivePosition) =
                (from.p manhattanDistanceTo to.p) + (from.level - to.level).absoluteValue * 500

            override fun cost(from: RecursivePosition, to: RecursivePosition) = 1
        }

        val start = RecursivePosition(0, start)
        val end = RecursivePosition(0, end)

        val solution = graph.AStarSearch(start, end).buildStack()
        return solution.size - 1
    }

    private fun Point.isOuterPortal() = x == 2 || y == 2 || x == area.second.x - 2 || y == area.second.y - 2
    private fun Point.isInnerPortal() = !isOuterPortal()

}

fun main() {
    Day20().run()
}