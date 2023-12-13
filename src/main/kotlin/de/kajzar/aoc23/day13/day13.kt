package de.kajzar.aoc23.day13

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day10.Grid
import de.kajzar.aoc23.day5.split

fun main() {
    val grids: List<Grid> = Day(13)
        .input()
        .readLines()
        .split { it.isBlank() }
        .map { s -> s.map { it.toCharArray().toList() } }

    // part 1
    grids
        .sumOf { g -> g.hLines().sumOf { it.hScore() } + g.vLines().sumOf { it.vScore() } }
        .also { println("Part 1 result: $it") }

    // part 2
    grids
        .map { it to it.replaceSmudge() }
        .sumOf { (grid, updatedGrid) ->
            val changedHLines = updatedGrid.hLines() - grid.hLines()
            val changedVLines = updatedGrid.vLines() - grid.vLines()
            changedHLines.sumOf { it.hScore() } + changedVLines.sumOf { it.vScore() }
        }
        .also { println("Part 2 result: $it") }

}

data class Line(val left: Int, val right: Int)

fun Line.vScore() = left + 1
fun Line.hScore() = (left + 1) * 100

fun Grid.replaceSmudge(): Grid {
    val originalHLines = hLines()
    val originalVLines = vLines()

    val mutations = adjustingCols() + adjustingRows()

    return mutations.first { updatedGrid ->
        val hLines = updatedGrid.hLines()
        val vLines = updatedGrid.vLines()

        (hLines.isNotEmpty() || vLines.isNotEmpty())
                && !(originalHLines == hLines && originalVLines == vLines)
    }
}

private fun Grid.adjustingRows() = sequence {
    for (p in rowPairs()) {
        val a = p.first()
        val b = p.last()

        val diff = diff(row(a), row(b)).singleOrNull()
        if (diff != null) {
            yield(replace(diff.newChar, a, diff.idx))
        }
    }
}

private fun Grid.adjustingCols() = sequence {
    for (p in colPairs()) {
        val a = p.first()
        val b = p.last()

        val diff = diff(col(a), col(b)).singleOrNull()
        if (diff != null) {
            yield(replace(diff.newChar, diff.idx, a))
        }
    }
}

fun Grid.replace(newChar: Char, rowIndex: Int, colIndex: Int): Grid {
    return mapIndexed { rIdx, row ->
        if (rIdx == rowIndex) {
            row.mapIndexed { cIdx, c ->
                if (cIdx == colIndex) {
                    newChar
                } else {
                    c
                }
            }
        } else {
            row
        }
    }
}


private fun pairs(indices: IntRange) = buildList {
    for (a in indices) {
        for (b in a..indices.last) {
            add(setOf(a, b))
        }
    }
}

fun Grid.colPairs() = pairs(cols())

fun Grid.rowPairs() = pairs(rows())

data class Diff(val oldChar: Char, val newChar: Char, val idx: Int)

fun diff(a: List<Char>, b: List<Char>) = buildList {
    for (i in a.indices) {
        val vA = a[i]
        val vB = b[i]
        if (vA != vB)
            add(Diff(vA, vB, i))
    }
}


private fun lines(indices: IntRange, accessor: (Int) -> List<Char>): Set<Line> {
    return indices
        .possibleLinePositions()
        .filter { l ->
            l.pairs(indices)
                .all { (l, r) -> accessor(l) == accessor(r) }
        }
        .toSet()
}

fun Grid.vLines() = lines(cols(), this::col)

fun Grid.hLines() = lines(rows(), this::row)

private fun Line.pairs(indices: IntRange) = sequence {
    var l = left
    var r = right
    while (true) {
        if (l in indices && r in indices) {
            yield(l to r)
            l--
            r++
        } else {
            break
        }
    }
}

private fun IntRange.possibleLinePositions() = windowed(2).map { (a, b) -> Line(a, b) }

fun Grid.cols() = first().indices
fun Grid.rows() = indices

fun Grid.row(idx: Int) = get(idx)
fun Grid.col(idx: Int) = map { it[idx] }