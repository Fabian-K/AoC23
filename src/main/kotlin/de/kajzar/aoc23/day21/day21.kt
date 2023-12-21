package de.kajzar.aoc23.day21

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day10.*
import de.kajzar.aoc23.day14.rows
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow

fun main() {
    val grid = Day(21)
        .input()
        .readLines()
        .map { s -> s.toCharArray().toList() }

    // part 1
    grid.reachableWithSteps(grid.find { it == 'S' }, 64)
        .also { println("Part 1 result: ${it.count()}") }

    // part 2
    grid.reachableWithStepsUsingPattern(26501365)
        .also { println("Part 2 result: %.0f".format(it)) }
}

data class State(val position: Position, val remainingSteps: Int)

private fun Grid.reachableWithSteps(
    start: Position,
    count: Int,
): Set<Position> {
    val queue: Queue<State> = LinkedList<State>().apply {
        add(State(start, count))
    }

    val known = mutableSetOf<State>()
    val result = mutableSetOf<Position>()

    while (queue.isNotEmpty()) {
        val state = queue.remove()

        if (!known.add(state)) continue

        if (state.remainingSteps == 0) {
            result.add(state.position)
            continue
        }

        for ((_, position) in state.position.neighbors().filter { this.isPartOfGrid(it.value) }) {
            val c = get(position)
            if (c != '#') {
                queue.add(State(position, state.remainingSteps - 1))
            }
        }
    }
    return result
}

private fun Grid.reachableWithStepsUsingPattern(steps: Int): Double {

    // resulting diamond shape follows a pattern with 14 different types
    // - 4 corners
    // - 8 edges (4 sides with two variants each)
    // - 2 fully filled
    // => compute count for each type and then multiply by occurrence of type

    val diamondWidth = (steps * 2) + 1

    var gridRepeatCount = ceil(diamondWidth.toDouble() / width()).toInt()
    if (gridRepeatCount % 2 == 0)
        gridRepeatCount += 1

    val totalGridWidth = width() * gridRepeatCount
    val emptySpaceOnEdge = width() - ((totalGridWidth - diamondWidth) / 2)

    val n = (ceil(gridRepeatCount / 2.0) - 1).toInt()

    // counts for full
    val countFullyA = if (n % 2 == 0) (n - 1).toDouble().pow(2) else n.toDouble().pow(2)
    val countFullyB = if (n % 2 == 0) n.toDouble().pow(2) else (n - 1).toDouble().pow(2)

    // counts for side (a & b)
    val countSideA = n
    val countSideB = max(n - 1, 0)

    // relevant positions
    val first = 0
    val last = width() - 1
    val center = width() / 2

    val topLeft = Position(first, first)
    val topRight = Position(last, first)
    val bottomLeft = Position(first, last)
    val bottomRight = Position(last, last)
    val centerOfGrid = Position(center, center)

    return listOf(
        // sides, for each variant (a & b)
        // TOP LEFT
        countSideA * countReachable(bottomRight, emptySpaceOnEdge - centerToEdge() - 2),
        countSideB * countReachable(bottomRight, emptySpaceOnEdge + centerToEdge() - 1),
        // TOP RIGHT
        countSideA * countReachable(bottomLeft, emptySpaceOnEdge - centerToEdge() - 2),
        countSideB * countReachable(bottomLeft, emptySpaceOnEdge + centerToEdge() - 1),
        // BOTTOM LEFT
        countSideA * countReachable(topRight, emptySpaceOnEdge - centerToEdge() - 2),
        countSideB * countReachable(topRight, emptySpaceOnEdge + centerToEdge() - 1),
        // BOTTOM RIGHT
        countSideA * countReachable(topLeft, emptySpaceOnEdge - centerToEdge() - 2),
        countSideB * countReachable(topLeft, emptySpaceOnEdge + centerToEdge() - 1),

        // corners
        1 * countReachable(Position(center, last), emptySpaceOnEdge - 1),
        1 * countReachable(Position(center, first), steps = emptySpaceOnEdge - 1),
        1 * countReachable(Position(last, center), steps = emptySpaceOnEdge - 1),
        1 * countReachable(Position(first, center), steps = emptySpaceOnEdge - 1),

        // fully-filled center variant (a & b)
        countFullyA * countReachable(centerOfGrid, 2 * centerToEdge() - 1),
        countFullyB * countReachable(centerOfGrid, 2 * centerToEdge()),
    )
        .sum()
}

fun Grid.width() = first().count()

fun Grid.centerToEdge() = width() / 2

private fun Grid.countReachable(position: Position, steps: Int): Double =
    reachableWithSteps(position, steps).count().toDouble()

fun Grid.find(p: (c: Char) -> Boolean): Position {
    for ((y, row) in rows().withIndex()) {
        for ((x, c) in row.withIndex()) {
            if (p(c)) return Position(x, y)
        }
    }
    error("Not found")
}
