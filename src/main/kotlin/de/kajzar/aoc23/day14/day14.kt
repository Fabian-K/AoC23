package de.kajzar.aoc23.day14

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day10.Grid
import de.kajzar.aoc23.day13.col
import de.kajzar.aoc23.day13.colIdxs

fun main() {
    val grid: Grid = Day(14)
        .input()
        .readLines()
        .map { s -> s.toCharArray().toList() }

    // part 1
    grid.tilt(Direction.NORTH)
        .calculateLoad()
        .let { println("Part 1 result: $it") }

    // part 2
    grid.cycle(1_000_000_000)
        .calculateLoad()
        .let { println("Part 2 result: $it") }
}

enum class Direction { NORTH, EAST, SOUTH, WEST }

fun Grid.calculateLoad(): Int = withIndex().sumOf { (i, row) ->
    val factor = count() - i
    row.count { it == 'O' } * factor
}

private val cache = mutableMapOf<Grid, Grid>()

fun Grid.cycle(targetCount: Int): Grid {
    var current = this
    var cycled = 0
    do {
        val remainingCount = targetCount - cycled

        // check if cycle can be skipped
        val cycleCount = knownCycleCountFor(current)
        if (cycleCount != null && cycleCount <= remainingCount) {
            val skipCycles = remainingCount / cycleCount
            cycled += skipCycles
        } else {
            current = current.cycle()
            cycled++
        }
    } while (cycled != targetCount)
    return current
}

fun knownProgression(grid: Grid): Sequence<Grid> = sequence {
    var current: Grid? = grid
    while (true) {
        current = cache[current]
        if (current == null)
            return@sequence
        yield(current)
    }
}

fun knownCycleCountFor(grid: Grid): Int? {
    val count = knownProgression(grid)
        .takeWhile { it != grid }
        .count()

    if (count == 0)
        return null
    return count
}

fun Grid.cycle(): Grid = cache.getOrPut(this) {
    tilt(Direction.NORTH)
        .tilt(Direction.WEST)
        .tilt(Direction.SOUTH)
        .tilt(Direction.EAST)
}

private fun Grid.tilt(direction: Direction): Grid {
    val lanes = when (direction) {
        Direction.NORTH, Direction.SOUTH -> columns()
        Direction.EAST, Direction.WEST -> rows()
    }

    val tilted = lanes.map { l ->
        val laneParts = l.sublist { it == '#' }
        laneParts
            .map { it.tiltPart(direction) }
            .flatten()
    }

    return when (direction) {
        Direction.NORTH, Direction.SOUTH -> tilted.colsToGrid()
        Direction.EAST, Direction.WEST -> tilted
    }
}

private fun List<Char>.tiltPart(direction: Direction): List<Char> {
    if (this == listOf('#'))
        return this

    val movingRocks = count { it == 'O' }
    val empty = count { it == '.' }
    val tilted = "O".repeat(movingRocks) + ".".repeat(empty)

    return when (direction) {
        Direction.NORTH, Direction.WEST -> tilted
        Direction.EAST, Direction.SOUTH -> tilted.reversed()
    }.toCharArray().toList()
}

fun List<List<Char>>.colsToGrid(): Grid {
    val rows = first().indices
    val result = mutableListOf<List<Char>>()

    for (i in rows) {
        val row = map { it[i] }
        result.add(row)
    }

    return result
}

fun Grid.columns() = colIdxs().map { col(it) }
fun Grid.rows() = this

fun <T> List<T>.sublist(pred: (T) -> Boolean): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var next = mutableListOf<T>()
    forEach { l ->
        if (pred(l)) {
            result.add(next.toList())
            result.add(listOf(l))
            next = mutableListOf()
        } else {
            next.add(l)
        }
    }
    result.add(next)
    return result
}