package de.kajzar.aoc23.day18

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day10.*
import java.io.BufferedReader
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun main() {
    // part 1
    Day(18)
        .input()
        .instructionsPart1()
        .content()
        .let { println("Part 1 result: %.0f".format(it)) }

    // part 2
    Day(18)
        .input()
        .instructionsPart2()
        .content()
        .let { println("Part 2 result: %.0f".format(it)) }
}


data class DigInstruction(
    val direction: Direction,
    val distance: Int,
)

private fun List<DigInstruction>.content(): Double {
    val boundary = digTrenches(this)
    val compressedGrid = boundary.compressedGrid()

    val totalArea = compressedGrid.totalArea()

    val grid = compressedGrid.map { row -> row.map { it.valueFromBoundary(boundary) } }
    val outsideSum = grid.reachableFrom(Position(0, 0))
        .sumOf { compressedGrid.get(it).weight() }
    return totalArea - outsideSum
}

private fun Boundary.compressedGrid(): CompressedGrid {
    val corners = map { it.from }.toSet()

    val xIndices = corners.map { it.x }.toSet().padded()
    val yIndices = corners.map { it.y }.toSet().padded()

    val xRanges = computeRanges(xIndices)
    val yRanges = computeRanges(yIndices)

    return buildList {
        for (y in yRanges) {
            val row = mutableListOf<Cell>()
            for (x in xRanges) {
                row.add(Cell(x, y))
            }
            add(row)
        }
    }
}

private fun Set<Int>.padded(): List<Int> {
    val min = min()
    val max = max()
    return this.plus(min - 1).plus(max + 1)
        .sorted()
}

typealias Boundary = Set<Edge>

typealias CompressedGrid = List<List<Cell>>

fun CompressedGrid.totalArea(): Double {
    val firstCell = first().first()
    val lastCell = last().last()

    val dx = abs(firstCell.xRange.first.toDouble()) + abs(lastCell.xRange.last) + 1
    val dy = abs(firstCell.yRange.first.toDouble()) + abs(lastCell.yRange.last) + 1

    return dx * dy
}

fun Cell.valueFromBoundary(trench: Boundary): Char {
    return when (type()) {
        CellType.Point, CellType.Line -> {
            val matching = trench.any { xRange.first in it.xRange() && yRange.first in it.yRange() }
            if (matching) {
                '#'
            } else {
                '.'
            }
        }

        CellType.Area -> '.'
    }
}

fun Edge.xRange() = min(from.x, to.x)..max(from.x, to.x)
fun Edge.yRange() = min(from.y, to.y)..max(from.y, to.y)
fun CompressedGrid.get(position: Position): Cell {
    return this[position.y][position.x]
}

enum class CellType {
    Point, Line, Area
}

private fun computeRanges(indices: List<Int>): List<IntRange> {
    val borderRanges = indices.map { it..it }
        .sortedBy { it.first }

    val gaps = mutableListOf<IntRange>()
    borderRanges.windowed(2).forEach { (a, b) ->
        if (a.last + 1 != b.first) {
            gaps.add(a.last + 1 until b.first)
        }
    }

    return borderRanges.plus(gaps)
        .sortedBy { it.first }
}

data class Cell(
    val xRange: IntRange,
    val yRange: IntRange,
)

fun Cell.weight() = xRange.count().toDouble() * yRange.count()

fun Cell.type(): CellType {
    return when {
        xRange.isSingleValue() && yRange.isSingleValue() -> CellType.Point
        xRange.isSingleValue() || yRange.isSingleValue() -> CellType.Line
        else -> CellType.Area
    }
}

private fun IntRange.count() = if (isSingleValue()) 1 else {
    if (last > first) (last - first) + 1 else (first - last) + 1
}

private fun IntRange.isSingleValue() = first == last

fun Grid.reachableFrom(position: Position): Set<Position> {
    val queue = Stack<Position>().apply {
        add(position)
    }
    val found = mutableSetOf<Position>()

    while (queue.isNotEmpty()) {
        val current = queue.pop()

        val value = get(current)
        if (value == '#') continue

        if (found.add(current)) {
            current.neighbors()
                .filter { (_, p) -> isPartOfGrid(p) }
                .forEach { (_, p) -> queue.add(p) }
        }
    }

    return found
}

data class Edge(
    val from: Position,
    val to: Position,
)

fun digTrenches(instructions: List<DigInstruction>): Boundary = buildSet {
    var currentPosition = Position(0, 0)

    for ((direction, distance) in instructions) {
        val next = currentPosition.move(direction, distance)
        add(Edge(currentPosition, next))
        currentPosition = next
    }
}

private fun Position.move(
    direction: Direction,
    distance: Int
): Position {
    return when (direction) {
        Direction.L -> this.copy(x = x - distance)
        Direction.R -> this.copy(x = x + distance)
        Direction.U -> this.copy(y = y - distance)
        Direction.D -> this.copy(y = y + distance)
    }
}

enum class Direction {
    L, R, U, D
}

private fun BufferedReader.instructionsPart1(): List<DigInstruction> = readLines()
    .map { it.split(" ") }
    .map { (dir, dist, _) ->
        DigInstruction(
            direction = Direction.valueOf(dir),
            distance = dist.toInt()
        )
    }

@OptIn(ExperimentalStdlibApi::class)
private fun BufferedReader.instructionsPart2(): List<DigInstruction> = readLines()
    .map { it.split(" ") }
    .map { (_, _, color) ->
        val chars = color.removePrefix("(#").removeSuffix(")")

        DigInstruction(
            direction = chars.last().digitToInt().toDirection(),
            distance = chars.take(5).hexToInt(),
        )
    }

private fun Int.toDirection() = when (this) {
    0 -> Direction.R
    1 -> Direction.D
    2 -> Direction.L
    3 -> Direction.U
    else -> error("Unknown direction $this")
}