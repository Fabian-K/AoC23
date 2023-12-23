package de.kajzar.aoc23.day23

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day10.*
import de.kajzar.aoc23.day13.colIdxs
import de.kajzar.aoc23.day13.rowIdxs

fun main() {
    val grid: Grid = Day(23)
        .input()
        .readLines()
        .map { s -> s.toCharArray().toList() }

    val start = Position(1, 0)
    val target = Position(grid.colIdxs().last - 1, grid.rowIdxs().last)

    grid.longestPath(start, target)
        .also { println("Part 1 result: ${it.count() - 1}") }

    val (_, edges) = grid.toGraph(start)
        .let { (nodes, edges) -> compress(nodes, edges) }

    findPaths(
        start = start,
        isTarget = { it == target },
        edges = edges,
    )
        .maxOf { it -> it.sumOf { it.length } }
        .also { println("Part 2 result: $it") }
}

fun compress(nodes: List<Node>, edges: List<Edge>): Pair<List<Node>, List<Edge>> {

    val remainingNodes = nodes.toMutableList()
    val remainingEdges = edges.toMutableList()

    val special = nodes.relevantNodes(edges)

    for (specialNode in special) {
        val paths = findPaths(
            start = specialNode,
            isTarget = { it in special.minus(specialNode) },
            edges = remainingEdges,
        ).filter { it -> it.all { it.length == 1 } } // ignore already compressed

        for (path in paths) {
            val target = path.last().to

            val nodesWithinPath = path.map { it.to }.dropLast(1)

            remainingNodes.removeAll(nodesWithinPath)
            remainingEdges.removeIf { e -> nodesWithinPath.any { e.contains(it) } }

            remainingEdges.add(Edge(specialNode, target, path.count()))
            remainingEdges.add(Edge(target, specialNode, path.count()))
        }
    }

    return remainingNodes to remainingEdges
}

fun findPaths(
    start: Node,
    isTarget: (Node) -> Boolean,
    edges: List<Edge>,
): List<List<Edge>> {
    val queue = mutableListOf<Pair<Node, List<Edge>>>().apply { add(start to emptyList()) }
    val result = mutableListOf<List<Edge>>()

    while (queue.isNotEmpty()) {
        val (current, paths) = queue.removeLast()

        if (isTarget(current)) {
            result.add(paths)
            continue
        }

        val nodesOnPath = listOf(start).plus(paths.map { it.to })

        val nextEdges = edges.filter { it.from == current }
            .filter { it.to !in nodesOnPath }

        for (edge in nextEdges) {
            val next = edge.to
            queue.add(next to paths + edge)
        }
    }
    return result
}

private fun List<Node>.relevantNodes(edges: List<Edge>) =
    filter { n -> edges.count { it.contains(n) } != 4 }

typealias Node = Position

data class Edge(val from: Node, val to: Node, val length: Int)

fun Edge.contains(pos: Position): Boolean = from == pos || to == pos

fun Grid.toGraph(start: Position): Pair<List<Node>, List<Edge>> {
    val queue = mutableListOf(start)
    val visited = mutableSetOf<Position>()

    val nodes = mutableListOf<Node>()
    val edges = mutableListOf<Edge>()

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current in visited) continue

        visited.add(current)
        nodes.add(current)

        current.neighbors()
            .filter { (_, p) -> isPartOfGrid(p) }
            .filter { (_, p) -> get(p) != '#' }
            .forEach { (_, p) ->
                edges.add(Edge(current, p, 1))
                queue.add(p)
            }
    }

    return nodes to edges
}

data class State(val path: List<Position>)

fun Grid.longestPath(start: Position, target: Position): List<Position> {
    val queue = mutableListOf(State(listOf(start)))

    val found = mutableListOf<State>()

    while (queue.isNotEmpty()) {

        val current = queue.removeFirst()
        val pos = current.path.last()
        if (pos == target) {
            found.add(current)
            continue
        }

        // next
        when (get(pos)) {
            '>' -> {
                val next = Position(x = pos.x + 1, y = pos.y)
                if (next !in current.path) {
                    queue.add(current.copy(path = current.path + next))
                }
            }

            '<' -> {
                val next = Position(x = pos.x - 1, y = pos.y)
                if (next !in current.path) {
                    queue.add(current.copy(path = current.path + next))
                }
            }

            '^' -> {
                val next = Position(x = pos.x, y = pos.y - 1)
                if (next !in current.path) {
                    queue.add(current.copy(path = current.path + next))
                }
            }

            'v' -> {
                val next = Position(x = pos.x, y = pos.y + 1)
                if (next !in current.path) {
                    queue.add(current.copy(path = current.path + next))
                }
            }

            else -> {
                pos.neighbors()
                    .filter { (_, p) -> isPartOfGrid(p) }
                    .filter { (_, p) -> get(p) != '#' }
                    .filter { (_, p) -> p !in current.path }
                    .forEach { (_, p) ->
                        queue.add(current.copy(path = current.path + p))
                    }
            }
        }
    }
    return found.maxBy { it.path.count() }
        .path
}