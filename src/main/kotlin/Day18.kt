import util.Dijkstra
import util.Graph
import util.breadthFirstSearch
import util.completeAcyclicTraverse

class Day18(testData: List<String>? = null) : Day<String>(18, 2019, ::asStrings, testData) {

    val map = input.map { it.toList() }
    val area = origin to (map[0].size - 1 to map.size - 1)
    val starts = area.allPoints().filter { map[it.y][it.x] == '@' }

    val allKeys = area.allPoints().filter { map[it.y][it.x] in 'a'..'z' }.map { map[it.y][it.x] }.toSet()

    data class State(val pos: Point, val keys: Set<Char>)

    operator fun List<List<Char>>.get(p: Point) = this[p.y][p.x]

    val graph = object : Graph<State> {
        override fun neighborsOf(node: State) = node.pos.neighbors()
            .filter {
                val c = map[it.y][it.x]
                c == '.' || c == '@' || c in 'a'..'z' || (c in 'A'..'Z' && c.toLowerCase() in node.keys)
            }.map {
                val c = map[it.y][it.x]
                State(it, node.keys + if (c in 'a'..'z') setOf(c) else emptySet())
            }
    }

    override fun part1(): Int {
        return 0
        val start = area.allPoints().filter { map[it] == '@' }.single()
        val solution = graph.breadthFirstSearch<State>(State(start, emptySet())) { it.keys == allKeys }
        return solution.size - 1
    }

    fun getAllKeys(start: Point, keys: Set<Char>) = graph.completeAcyclicTraverse<State>(State(start, keys))

    data class State4(val steps: Int, val pos: List<Point>, val keys: Set<Char>)

    data class MState(val keys: Set<Char>, val pos: List<Point>)

    override fun part2(): Int? {
        println(starts.toList())

        val startM = MState(emptySet(), starts.toList())
        val d = Dijkstra(::neighborsOf, ::cost)
        val result = d.search(startM) { it.keys == allKeys }

        return result.second.first[result.first]
    }

    val costCache = mutableMapOf<Pair<MState, MState>, Int>()

    val keysWithDepthCache = listOf(
        mutableMapOf<State, List<Pair<Pair<Point, Char>, Int>>>(),
        mutableMapOf<State, List<Pair<Pair<Point, Char>, Int>>>(),
        mutableMapOf<State, List<Pair<Pair<Point, Char>, Int>>>(),
        mutableMapOf<State, List<Pair<Pair<Point, Char>, Int>>>()
    )


    fun neighborsOf(node: MState): Collection<MState> {
        val r = node.pos.mapIndexed { idx, p ->
            val single = State(p, node.keys intersect relevantKeysFor[idx])
            val keysWithDepth = keysWithDepthCache[idx].getOrPut(single) {
                val layers = graph.completeAcyclicTraverse(single)
                val reachableKeys =
                    layers
                        .map { it.second }.flatten().map { it.pos to map[it.pos] }
                        .filter { it.second in 'a'..'z' && it.second !in node.keys }
                        .distinct().toList()

                reachableKeys.map { k: Pair<Point, Char> ->
                    k to layers.first { l: Pair<Int, Set<State>> ->
                        k.second in l.second.flatMap { s -> s.keys }
                    }.let { it.first }
                }
            }
            keysWithDepth.map {
                val newState = MState(node.keys + it.first.second, node.pos.change(idx, it.first.first))
                costCache.put(node to newState, it.second)
                newState
            }
        }.flatten()

//        println("Looking at $node")
//        r.forEach {
//            println("${costCache[node to it]}-> $it")
//        }

        return r
    }

    fun cost(node1: MState, node2: MState): Int {
        return costCache[node1 to node2]!!
    }

    fun <T> List<T>.change(idx: Int, v: T): List<T> = toMutableList().apply { this[idx] = v }

    val graph4 = object : Graph<State4> {
        override fun neighborsOf(node: State4): Collection<State4> {
            val r = node.pos.mapIndexed { idx, p ->
                val single = State(p, node.keys)
                val layers = graph.completeAcyclicTraverse(single)
                val reachable =
                    layers
                        .map { it.second }.flatten().map { it.pos to map[it.pos] }
                        .distinct().toList()

                val keys = reachable.filter { it.second in 'a'..'z' && it.second !in node.keys }
//                val blockedBy =
//                    reachable.map { it.first.neighbors() }.flatten().distinct().map { it to map[it] }
//                        .filter { it.second in 'A'..'Z' && it.second.toLowerCase() !in node.keys }
//                println("$p -> blocked by: $blockedBy and can reach $keys")

                keys.map { (pos, key) ->
                    val solution = graph.breadthFirstSearch<State>(single) { it.pos == pos }

                    State4(
                        node.steps + solution.size - 1,
                        node.pos.change(idx, pos),
                        node.keys + solution.lastElement().keys
                    )
                }
            }.flatten()
            println(r)
            return r
            return node.pos.mapIndexedNotNull { idx, p ->
                //ask if this robot can do anything!
                val single = State(p, node.keys)

                val reachable =
                    graph.completeAcyclicTraverse(single).map { it.second.filter { map[it.pos] in 'a'..'z' } }
                        .flatten()
                        .toList()

                val keys = reachable.map { map[it.pos] } - node.keys

                if (keys.isEmpty())
                    null
                else
                    p.neighbors()
                        .filter {
                            val c = map[it]
                            c == '.' || c in 'a'..'z' || (c in 'A'..'Z' && c.toLowerCase() in node.keys)
                        }.map {
                            val c = map[it]
                            State4(
                                node.steps,
                                node.pos.toMutableList().apply { this[idx] = it },
                                node.keys + if (c in 'a'..'z') setOf(c) else emptySet()
                            )
                        }
            }.flatten()
        }
    }

    private val keysIn = allKeys.associateWith { key ->
        val p = area.allPoints().filter { map[it] == key }.single()
        when {
            p.x < 40 && p.y < 40 -> 0
            p.x > 40 && p.y < 40 -> 1
            p.x < 40 && p.y > 40 -> 2
            p.x > 40 && p.y > 40 -> 3
            else -> error("")
        }
    }

    private val doorsIn = allKeys.map { it.toUpperCase() }.associateWith { key ->
        val p = area.allPoints().filter { map[it] == key }.single()
        when {
            p.x < 40 && p.y < 40 -> 0
            p.x > 40 && p.y < 40 -> 1
            p.x < 40 && p.y > 40 -> 2
            p.x > 40 && p.y > 40 -> 3
            else -> error("")
        }
    }

    private val relevantKeysFor = (0..3).map { seg ->
        doorsIn.filter { (door, idx) -> idx == seg }.map { it.key.toLowerCase() }.toSet()
    }
}

fun main() {
    Day18().run()
}