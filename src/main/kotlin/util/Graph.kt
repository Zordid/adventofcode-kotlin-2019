package util

import java.util.*

interface Graph<N> {
    fun neighborsOf(node: N): Collection<N>
}

fun <N> Graph<N>.breadthFirstSearch(start: N, destPredicate: (N) -> Boolean): Stack<N> =
    breadthFirstSearch(start, ::neighborsOf, destPredicate)

fun <N> Graph<N>.breadthFirstSearch(start: N, dest: N): Stack<N> =
    breadthFirstSearch(start, ::neighborsOf, { it == dest })

fun <N> Graph<N>.depthFirstSearch(start: N, destPredicate: (N) -> Boolean): Stack<N> =
    depthFirstSearch(start, ::neighborsOf, destPredicate)

fun <N> Graph<N>.depthFirstSearch(start: N, dest: N): Stack<N> =
    depthFirstSearch(start, ::neighborsOf, { it == dest })

fun <N> Graph<N>.completeAcyclicTraverse(start: N) =
    SearchEngineWithNodes(::neighborsOf).completeAcyclicTraverse(start)

fun <N> Graph<N>.AStarSearch(start: N, dest: N, cost: (N, N) -> Int, costEstimate: (N, N) -> Int) =
    buildStack(dest, AStar(::neighborsOf, cost, costEstimate).search(start, dest))
