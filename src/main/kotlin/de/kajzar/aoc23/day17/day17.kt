package de.kajzar.aoc23.day17

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day10.Position
import de.kajzar.aoc23.day10.isPartOfGrid
import de.kajzar.aoc23.day11.Grid
import de.kajzar.aoc23.day11.toGrid

fun main() {

    val grid = Day(17)
        .input()
        .readText()
        .toGrid()

    // part 1
    findPath(
        start = Node(Position(0, 0), null),
        isTarget = { it.position == Position(140, 140) },
        nextEdgesFrom = allowedMovementFor(grid, 1..3),
        weightFor = { it.heatLoss(grid) },
    )
        .heatLoss(grid)
        .let { println("Part 1 result: $it") }

    // part 2
    findPath(
        start = Node(Position(0, 0), null),
        isTarget = { it.position == Position(140, 140) },
        nextEdgesFrom = allowedMovementFor(grid, 4..10),
        weightFor = { it.heatLoss(grid) },
    )
        .heatLoss(grid)
        .let { println("Part 2 result: $it") }
}

data class Node(
    val position: Position,
    val from: Direction?,
)

data class Edge(
    val from: Node,
    val to: Node,
)

enum class Direction {
    LEFT, TOP, RIGHT, BOTTOM;
}

fun Direction.opposite(): Direction = when (this) {
    Direction.LEFT -> Direction.RIGHT
    Direction.TOP -> Direction.BOTTOM
    Direction.RIGHT -> Direction.LEFT
    Direction.BOTTOM -> Direction.TOP
}

fun Position.move(direction: Direction, by: Int): Position = when (direction) {
    Direction.LEFT -> Position(x - by, y)
    Direction.TOP -> Position(x, y - by)
    Direction.RIGHT -> Position(x + by, y)
    Direction.BOTTOM -> Position(x, y + by)
}

private fun allowedMovementFor(grid: Grid, stepRange: IntRange): (Node) -> List<Edge> = { node ->
    val (pos, fromDirection) = node
    val directions = allowedDirections(fromDirection)

    buildList {
        stepRange.forEach { walkBy ->
            directions.forEach { d ->
                val newPos = pos.move(d, walkBy)
                if (grid.isPartOfGrid(newPos)) {
                    add(Edge(node, Node(newPos, d.opposite())))
                }
            }
        }
    }
}

private fun allowedDirections(source: Direction?): List<Direction> {
    return Direction.entries
        .minus(source)
        .minus(source?.opposite())
        .filterNotNull()
}

private fun Edge.heatLoss(grid: Grid) = positions().drop(1).sumOf { grid.get(it) }

private fun List<Edge>.heatLoss(grid: Grid) = sumOf { it.heatLoss(grid) }

fun Grid.get(position: Position): Int {
    return this[position.y][position.x].toString().toInt()
}

fun Map<Node, Node?>.extractPathBetween(start: Node, end: Node): List<Edge> {
    val path = mutableListOf<Edge>()
    var current = end
    while (current != start) {
        val previous = getValue(current) ?: error("no path found")
        path.add(Edge(previous, current))
        current = previous
    }
    return path.reversed()
}

fun Edge.positions(): List<Position> {
    return when {
        from.position.y < to.position.y -> (from.position.y..to.position.y).map { Position(from.position.x, it) }
        from.position.y > to.position.y -> (from.position.y downTo to.position.y).map { Position(from.position.x, it) }
        from.position.x < to.position.x -> (from.position.x..to.position.x).map { Position(it, from.position.y) }
        from.position.x > to.position.x -> (from.position.x downTo to.position.x).map { Position(it, from.position.y) }
        else -> error("invalid edge: $this")
    }
}

fun findPath(
    start: Node,
    isTarget: (Node) -> Boolean,
    nextEdgesFrom: (Node) -> List<Edge>,
    weightFor: (Edge) -> Int,
): List<Edge> {
    val queue = mutableMapOf<Node, Int>().apply { this[start] = 0 }
    val predecessorMap = mutableMapOf<Node, Node?>().apply { this[start] = null }
    val costsToReach = mutableMapOf<Node, Int>().apply { this[start] = 0 }

    while (queue.isNotEmpty()) {
        val current = queue.minByOrNull { (_, cost) -> cost }!!.key
        queue.remove(current)

        if (isTarget(current)) {
            return predecessorMap.extractPathBetween(start, current)
        }

        for (edge in nextEdgesFrom(current)) {
            val next = edge.to
            val newCost = costsToReach.getValue(current) + weightFor(edge)

            if (newCost < (costsToReach[next] ?: Int.MAX_VALUE)) {
                costsToReach[next] = newCost
                queue[next] = newCost
                predecessorMap[next] = current
            }
        }
    }
    error("No path found")
}