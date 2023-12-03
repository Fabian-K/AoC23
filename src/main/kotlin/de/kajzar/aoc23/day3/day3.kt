package de.kajzar.aoc23.day3

import de.kajzar.aoc23.Day

fun main() {

    val matrix: Matrix = Day(3).input()
        .lines().toList()
        .map { line -> line.toCharArray().toList() }

    val symbols = matrix.symbols()
    val numbers = matrix.numbers()

    numbers
        .filter { number -> symbols.any { symbol -> number.isAdjacentToPoint(symbol) } }
        .sumOf { matrix.numberAt(it) }
        .let { println("Part 1 Sum: $it") }

    matrix.gears()
        // adjacent numbers
        .map { gear ->
            numbers.filter { it.isAdjacentToPoint(gear) }
                .map { matrix.numberAt(it) }
        }
        .filter { it.count() == 2 }
        .sumOf { it.reduce { a, b -> a * b } }
        .let { println("Part 2 Sum: $it") }
}

typealias Matrix = List<List<Char>>

typealias Point = Pair<Int, Int>

fun Point.row() = first
fun Point.col() = second

fun Point.isAdjacentTo(other: Point): Boolean {
    val rowRange = IntRange(row() - 1, row() + 1)
    val colRange = IntRange(col() - 1, col() + 1)
    return other.row() in rowRange
            && other.col() in colRange
}

fun Char.isSymbol() = !isDigit() && this != '.'
fun Char.isGear() = this == '*'

fun Matrix.symbols(): List<Point> = find { it.isSymbol() }
fun Matrix.gears(): List<Point> = find { it.isGear() }

fun Matrix.find(pred: (Char) -> Boolean): List<Point> = buildList {
    this@find.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { colIndex, c ->
            if (pred(c)) add(rowIndex to colIndex)
        }
    }
}

typealias Range = Pair<Point, Point> // start & end

fun Range.start() = first
fun Range.end() = second

fun Range.points(): Sequence<Point> = sequence {
    (start().row()..end().row()).forEach { row ->
        (start().col()..end().col()).forEach { col ->
            yield(row to col)
        }
    }
}

fun Range.isAdjacentToPoint(point: Point) = points().any { it.isAdjacentTo(point) }

fun Matrix.numberAt(range: Range): Int = range.points().map { this[it.row()][it.col()] }.joinToString(separator = "")
    .toInt()

fun Matrix.numbers(): List<Range> = buildList {
    this@numbers.forEachIndexed { rowIndex, row ->
        var start: Point? = null
        row.forEachIndexed { colIndex, it ->
            if (it.isDigit() && start == null) {
                start = rowIndex to colIndex
            }
            if (!it.isDigit() && start != null) {
                val end = rowIndex to colIndex - 1
                add(start!! to end)
                start = null
            }
        }
        if (start != null)
            add(start!! to (rowIndex to row.count() - 1))
    }
}