package de.kajzar.aoc23.day16

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day10.Position
import de.kajzar.aoc23.day10.get
import de.kajzar.aoc23.day11.Grid
import de.kajzar.aoc23.day11.toGrid
import de.kajzar.aoc23.day13.colIdxs
import de.kajzar.aoc23.day13.rowIdxs
import java.util.*

fun main() {
    val grid = Day(16)
        .input()
        .readText()
        .toGrid()

    // part 1
    println("Part 1 result: ${grid.computeEnergized(Position(0, 0), Input.L)}")

    // part 2
    allInputPossibilities(grid)
        .maxOf { (position, input) -> grid.computeEnergized(position, input) }
        .also { println("Part 2 result: $it") }
}

private fun allInputPossibilities(grid: Grid) = buildList {
    for (rowIdx in grid.rowIdxs()) {
        add(Position(0, rowIdx) to Input.L)
        add(Position(grid.colIdxs().last, rowIdx) to Input.R)
    }
    for (colIdx in grid.colIdxs()) {
        add(Position(colIdx, 0) to Input.T)
        add(Position(colIdx, grid.rowIdxs().last) to Input.B)
    }
}

private fun Grid.computeEnergized(startPosition: Position, startInput: Input): Int {
    val tracedInputs = mutableMapOf<Position, MutableSet<Input>>()

    val queue = Stack<Pair<Position, Input>>().also {
        it.push(startPosition to startInput)
    }

    while (queue.isNotEmpty()) {
        val (position, input) = queue.pop()

        if (position.x !in this.rowIdxs()) continue
        if (position.y !in this.colIdxs()) continue
        if (tracedInputs[position]?.contains(input) == true) continue

        tracedInputs.getOrPut(position) { mutableSetOf() }
            .add(input)

        val tile = this.get(position)

        when (tile) {
            '.' -> {
                when (input) {
                    Input.L -> queue.add(position.copy(x = position.x + 1) to Input.L)
                    Input.R -> queue.add(position.copy(x = position.x - 1) to Input.R)
                    Input.T -> queue.add(position.copy(y = position.y + 1) to Input.T)
                    Input.B -> queue.add(position.copy(y = position.y - 1) to Input.B)
                }
            }

            '|' -> {
                when (input) {
                    Input.L, Input.R -> queue.apply {
                        add(position.copy(y = position.y - 1) to Input.B)
                        add(position.copy(y = position.y + 1) to Input.T)
                    }

                    Input.T -> queue.add(position.copy(y = position.y + 1) to Input.T)
                    Input.B -> queue.add(position.copy(y = position.y - 1) to Input.B)
                }
            }

            '-' -> {
                when (input) {
                    Input.T, Input.B -> queue.apply {
                        add(position.copy(x = position.x - 1) to Input.R)
                        add(position.copy(x = position.x + 1) to Input.L)
                    }

                    Input.L -> queue.add(position.copy(x = position.x + 1) to Input.L)
                    Input.R -> queue.add(position.copy(x = position.x - 1) to Input.R)
                }
            }

            '/' -> {
                when (input) {
                    Input.L -> queue.add(position.copy(y = position.y - 1) to Input.B)
                    Input.R -> queue.add(position.copy(y = position.y + 1) to Input.T)
                    Input.T -> queue.add(position.copy(x = position.x - 1) to Input.R)
                    Input.B -> queue.add(position.copy(x = position.x + 1) to Input.L)
                }
            }

            '\\' -> {
                when (input) {
                    Input.L -> queue.add(position.copy(y = position.y + 1) to Input.T)
                    Input.R -> queue.add(position.copy(y = position.y - 1) to Input.B)
                    Input.T -> queue.add(position.copy(x = position.x + 1) to Input.L)
                    Input.B -> queue.add(position.copy(x = position.x - 1) to Input.R)
                }
            }
        }
    }

    return tracedInputs.count()
}

enum class Input {
    L, R, T, B
}
