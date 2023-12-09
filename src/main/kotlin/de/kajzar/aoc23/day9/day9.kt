package de.kajzar.aoc23.day9

import de.kajzar.aoc23.Day

fun main() {
    val sequences: List<List<Int>> = Day(9)
        .input()
        .lines().map { it.split(" ").map { it.toInt() } }.toList()

    sequences
        .sumOf { it.nextValue() }
        .let { println("Part 1 result: $it") }

    sequences
        .sumOf { it.previousValue() }
        .let { println("Part 2 result: $it") }
}

fun List<Int>.nextValue(): Int {
    return derivedSequencesFor(this)
        .takeWhile { it.any { e -> e != 0 } }
        .toList().reversed().fold(0) { acc, seq ->
            seq.last() + acc
        }
}

fun List<Int>.previousValue(): Int {
    return derivedSequencesFor(this)
        .takeWhile { it.any { e -> e != 0 } }
        .toList().reversed().fold(0) { acc, seq ->
            seq.first() - acc
        }
}

private fun derivedSequencesFor(sequence: List<Int>): Sequence<List<Int>> = sequence {
    var base = sequence
    while (true) {
        yield(base)
        base = base.windowed(2).map { (a, b) -> b - a }
    }
}
