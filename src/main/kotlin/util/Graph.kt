package util

import java.util.*

interface Graph<N> {
    fun neighborsOf(node: N): Collection<N>

    fun cost(from: N, to: N): Int = throw NotImplementedError("needs a cost fun")
    fun costEstimation(from: N, to: N): Int = throw NotImplementedError("needs a costEstimation fun")
}

fun <N> Graph<N>.breadthFirstSearch(start: N, destPredicate: (N) -> Boolean): Stack<N> =
    breadthFirstSearch(start, ::neighborsOf, destPredicate)

fun <N> Graph<N>.breadthFirstSearch(start: N, dest: N): Stack<N> =
    breadthFirstSearch(start, ::neighborsOf, { it == dest })

fun <N> Graph<N>.depthFirstSearch(start: N, destPredicate: (N) -> Boolean): Stack<N> =
    depthFirstSearch(start, ::neighborsOf, destPredicate)

fun <N> Graph<N>.depthFirstSearch(start: N, dest: N): Stack<N> =
    depthFirstSearch(start, ::neighborsOf, { it == dest })

fun <N> Graph<N>.completeAcyclicTraverse(start: N, safeMode: Boolean = true): Sequence<Set<N>> =
    SearchEngineWithNodes(::neighborsOf).completeAcyclicTraverse(start, safeMode)

fun <N> Graph<N>.completeBreadthFirstTraverse(start: N): Sequence<Set<N>> =
    SearchEngineWithNodes(::neighborsOf).completeBreadthFirstTraverse(start)

fun <N> Graph<N>.AStarSearch(start: N, dest: N) =
    buildStack(dest, AStar(::neighborsOf, ::cost, ::costEstimation).search(start, dest))

fun <N> Graph<N>.dijkstraSearch(start: N, dest: N) =
    Dijkstra<N>(::neighborsOf, ::cost).search(start, { it == dest })

fun <N> Graph<N>.dijkstraSearch(start: N, destPredicate: (N) -> Boolean) =
    Dijkstra<N>(::neighborsOf, ::cost).search(start, destPredicate)
