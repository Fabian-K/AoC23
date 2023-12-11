package de.kajzar.aoc23.day11

import de.kajzar.aoc23.Day
import kotlin.math.abs

fun main() {
    val grid = Day(11)
        .input()
        .readText().toGrid()

    val galaxies = grid.galaxies()

    val emptyRows = grid.emptyRows()
    val emptyCols = grid.emptyCols()

    galaxies.expand(rows = emptyRows, cols = emptyCols, expandBy = 1)
        .pairs()
        .sumOf { it.distance() }
        .let { println("Part 1 result: $it") }

    galaxies.expand(rows = emptyRows, cols = emptyCols, expandBy = 1000000 - 1)
        .pairs()
        .sumOf { it.distance() }
        .let { println("Part 2 result: $it") }
}

typealias Grid = List<List<Char>>

typealias Galaxy = Pair<Int, Int>

fun List<Galaxy>.expand(rows: List<Int>, cols: List<Int>, expandBy: Int): List<Galaxy> {
    return map { (x, y) ->
        val affectedByRow = rows.count { it < y }
        val affectedByCol = cols.count { it < x }

        val expandedX = x + (affectedByCol * expandBy)
        val expandedY = y + (affectedByRow * expandBy)
        expandedX to expandedY
    }
}

fun Grid.galaxies() = buildList {
    this@galaxies.forEachIndexed { y, row ->
        row.forEachIndexed { x, c ->
            if (c == '#') add(Galaxy(x, y))
        }
    }
}

fun Grid.emptyRows() = indices.filter { row(it).hasNoGalaxies() }
fun Grid.emptyCols() = first().indices.filter { col(it).hasNoGalaxies() }

fun List<Galaxy>.pairs() = buildSet {
    this@pairs.forEachIndexed { idx, a ->
        subList(idx + 1, this@pairs.size).forEach { b ->
            add(a to b)
        }
    }
}

fun Pair<Galaxy, Galaxy>.distance(): Long {
    val (x1, y1) = first
    val (x2, y2) = second

    return abs(x1 - x2).toLong() + abs(y1 - y2)
}

fun String.toGrid(): Grid {
    return lines().map { it.toCharArray().toList() }
}

fun Grid.row(idx: Int) = get(idx)
fun Grid.col(idx: Int) = map { it[idx] }

fun List<Char>.hasNoGalaxies() = all { it == '.' }