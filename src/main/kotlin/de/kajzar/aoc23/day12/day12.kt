package de.kajzar.aoc23.day12

import de.kajzar.aoc23.Day

fun main() {
    val rows = Day(12)
        .input()
        .lines().toList()
        .map { row ->
            val (seq, rec) = row.split(" ")
            Row(seq, rec.split(",").map { it.toInt() })
        }

    rows
        .sumOf { count(it) }
        .let { println("Part 1 result: $it") }

    rows
        .map { it.unfold() }
        .sumOf { count(it) }
        .let { println("Part 2 result: $it") }
}

fun count(row: Row, cache: MutableMap<Row, Long> = mutableMapOf()): Long = cache.getOrPut(row) {
    val simple = row.simplify() ?: return 0

    var count = 0L

    if (simple.sequence.any { it == '?' }) {
        // replace first as #
        count += count(Row(simple.sequence.replaceFirst('?', '#'), simple.records), cache)
        // replace first as .
        count += count(Row(simple.sequence.replaceFirst('?', '.'), simple.records), cache)
    } else {
        if (simple.sequence.matchesRecord(simple.records)) count++
    }

    count
}

data class Row(
    val sequence: String,
    val records: List<Int>,
)

// null if row is invalid
private fun Row.simplify(): Row? {
    if (this.sequence.first() == '.') {
        return Row(sequence.dropWhile { it == '.' }, records)
    }

    if (records.isEmpty() && sequence.any { it == '#' }) {
        return null
    }

    val knownParts = sequence.takeWhile { it != '?' }

    val knownSequence = knownParts.split('.')
        .map { it.count() }
        .dropLast(1)

    if (knownSequence.isNotEmpty()) {
        val actual = knownSequence.first()
        val required = records.first()

        if (actual == required) {
            val remaining = sequence.removePrefix("#".repeat(actual) + ".")
            return Row(remaining, records.drop(1))
        } else {
            return null
        }
    }

    // might already be past point
    val atLeast = knownParts.takeWhile { it == '#' }
    if (atLeast.count() > records.first())
        return null

    return this
}

private fun Row.unfold(): Row {
    val seq = buildList {
        repeat(5) {
            add(sequence)
        }
    }.joinToString("?")

    val records = buildList {
        repeat(5) {
            addAll(records)
        }
    }

    return Row(seq, records)
}

fun String.matchesRecord(records: List<Int>): Boolean {

    if (isEmpty() && records.isEmpty())
        return true

    val sequenceRecord = this.split('.')
        .filter { it.isNotEmpty() }
        .map { it.count() }

    return sequenceRecord == records
}