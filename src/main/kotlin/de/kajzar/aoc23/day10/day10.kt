package de.kajzar.aoc23.day10

import de.kajzar.aoc23.Day
import java.util.*


fun main() {
    val (grid, start) = Day(10)
        .input()
        .let {
            val grid = it.readText().toGrid()
            grid.replaceStart() to grid.start()
        }

    // part 1
    grid.loopFor(start)
        .let { println("Part 1 result: ${it.count() / 2}") }

    // part 2
    val mainLoop = grid.loopFor(start)

    val gridLoopOnly = grid.keepOnly(mainLoop)

    // "explode" grid to simplify squeeze through
    val explodedGrid = gridLoopOnly.explode()

    // find outside positions in exploded grid
    val outsidePositionsExploded = explodedGrid.withSimpleConnectionTo(Position(0, 0))

    // counting original empty positions
    val outsidePositions = outsidePositionsExploded
        // ignore all positions added via explosion
        .filter { p -> p.x % 3 == 1 && p.y % 3 == 1 }
        .filter { explodedGrid.get(it) == '.' }
        // re-map to non-exploded positions
        .map { it.copy(x = it.x / 3, y = it.y / 3) }
        .toSet()

    val insideCount = grid.totalCount() - mainLoop.count() - outsidePositions.count()
    println("Part 2 result: $insideCount")
}

typealias Grid = List<List<Char>>

fun Grid.totalCount() = count() * first().count()

data class Position(val x: Int, val y: Int)

fun Grid.get(p: Position): Char {
    return if (isPartOfGrid(p))
        this[p.y][p.x]
    else ' '
}

fun Grid.isPartOfGrid(position: Position): Boolean {
    return position.y in this.indices && position.x in first().indices
}

fun Grid.withSimpleConnectionTo(start: Position): Set<Position> {
    val queue = Stack<Position>().also {
        it.push(start)
    }
    val visited = mutableSetOf<Position>()
    val result = mutableSetOf<Position>().also {
        it.add(start)
    }

    while (!queue.isEmpty()) {
        val e = queue.pop()

        if (!visited.add(e)) continue

        val newNeighbors = e.neighbors().values
            .filter { get(e) == '.' }
            .filter { isPartOfGrid(it) }
            .filter { it !in visited }

        result.addAll(newNeighbors)
        queue.addAll(newNeighbors)
        visited.add(e)
    }

    return result
}

fun Grid.dump(marked: Set<Position> = emptySet(), other: (Char) -> Char = { it }, markWith: Char = 'X') {
    forEachIndexed { y, row ->
        row.forEachIndexed { x, c ->
            if (Position(x, y) in marked) {
                print(markWith)
            } else {
                print(other(c))
            }
        }
        println()
    }
    println()
}

fun Grid.start(): Position {
    forEachIndexed { y, line ->
        line.forEachIndexed { x, c ->
            if (c == 'S') return Position(x, y)
        }
    }
    error("")
}

enum class Direction { N, E, S, W }

fun Direction.inverted() = when (this) {
    Direction.N -> Direction.S
    Direction.E -> Direction.W
    Direction.S -> Direction.N
    Direction.W -> Direction.E
}

fun Position.neighbors() = Direction.entries.associateWith { moveTo(it) }

fun Position.moveTo(direction: Direction) = when (direction) {
    Direction.N -> copy(y = y - 1)
    Direction.S -> copy(y = y + 1)
    Direction.W -> copy(x = x - 1)
    Direction.E -> copy(x = x + 1)
}

fun Grid.replaceStart(): Grid {
    val start = start()
    val neighborsTo = start.neighbors()

    val connections = buildSet {
        Direction.entries.forEach { d ->
            if (hasPipeTo(neighborsTo[d]!!, d.inverted()))
                add(d)
        }
    }

    val startValue = pipeConnecting(connections.elementAt(0), connections.elementAt(1))

    return map { line ->
        line.map { c -> if (c == 'S') startValue else c }
    }
}

val pipes = setOf('|', '-', 'L', 'J', '7', 'F')
fun pipeConnecting(a: Direction, b: Direction): Char {
    return pipes.first { it.connectionsTo().containsAll(setOf(a, b)) }
}

fun Grid.hasPipeTo(position: Position, s: Direction): Boolean {
    val char = get(position)
    val connections = char.connectionsTo()
    return connections.contains(s)
}


fun Char.connectionsTo(): Set<Direction> {
    return when (this) {
        '|' -> setOf(Direction.N, Direction.S)
        '-' -> setOf(Direction.E, Direction.W)
        'L' -> setOf(Direction.N, Direction.E)
        'J' -> setOf(Direction.N, Direction.W)
        '7' -> setOf(Direction.S, Direction.W)
        'F' -> setOf(Direction.S, Direction.E)
        else -> emptySet()
    }
}

fun Grid.pipeSequence(start: Position, direction: Direction) = sequence {
    var position = start
    var nextDirection = direction
    while (true) {
        val next = position.moveTo(nextDirection)
        yield(next)
        nextDirection = this@pipeSequence.get(next).connectionsTo()
            .minus(nextDirection.inverted())
            .single()
        position = next
    }
}

fun Grid.keepOnly(positions: Set<Position>) = mapIndexed { y, row ->
    row.mapIndexed { x, chars ->
        val pos = Position(x, y)
        if (pos in positions)
            chars
        else
            '.'
    }
}

fun Grid.loopFor(start: Position): Set<Position> {
    val direction = get(start).connectionsTo().first()

    val visited = mutableSetOf<Position>()
    pipeSequence(start, direction)
        .takeWhile { visited.add(it) }
        .last()

    return visited
}

fun Grid.explode(): Grid = flatMap { row ->
    val exploded = row.map { c -> exploded(c) }
    // -> now 3 rows
    (0 until 3).map { i -> exploded.flatMap { it[i] } }
}

private fun exploded(c: Char) = when (c) {
    '|' -> """
                    .|.
                    .|.
                    .|.
                """.trimIndent().toGrid()

    '-' -> """
                    ...
                    ---
                    ...
                """.trimIndent().toGrid()

    'L' -> """
                    .|.
                    .L-
                    ...
                """.trimIndent().toGrid()

    'J' -> """
                    .|.
                    -J.
                    ...
                """.trimIndent().toGrid()

    '7' -> """
                    ...
                    -7.
                    .|.
                """.trimIndent().toGrid()

    'F' -> """
                    ...
                    .F-
                    .|.
                """.trimIndent().toGrid()

    else -> """
                    ...
                    ...
                    ...
                """.trimIndent().toGrid()
}

private fun String.toGrid(): Grid {
    return lines().map { it.toCharArray().toList() }
}