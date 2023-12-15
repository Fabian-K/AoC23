package de.kajzar.aoc23.day15

import de.kajzar.aoc23.Day

fun main() {
    val input = Day(15)
        .input()
        .readText()

    // part 1
    input.split(",")
        .sumOf { it.hash() }
        .also { println("Part 1 result: $it") }

    // part 2
    val boxes = (0..255).associateWith { BoxContent(java.util.LinkedHashMap()) }

    // apply operations
    input.split(",").forEach { opString ->
        if (opString.contains("=")) {
            val (label, number) = opString.split("=")
            boxes.getValue(label.hash()).add(label, number.toInt())
        } else {
            val label = opString.substringBefore("-")
            boxes.getValue(label.hash()).remove(label)
        }
    }

    // calculate sum
    boxes.values
        .mapIndexed { idx, boxContent -> boxContent.focusPower(idx) }
        .sum()
        .also { println("Part 2 result: $it") }

}

data class BoxContent(private var lenses: LinkedHashMap<String, Int>) {

    fun add(label: String, number: Int) {
        if (lenses[label] != null) {
            val newValues = lenses.map { (k, v) -> if (k == label) k to number else k to v }
            lenses = java.util.LinkedHashMap()
            for (e in newValues) {
                lenses[e.first] = e.second
            }
        } else {
            lenses[label] = number
        }
    }

    fun remove(label: String) {
        lenses.remove(label)
    }

    fun focusPower(boxIdx: Int): Int {
        val lensSum = lenses.values.mapIndexed { index, i -> i * (index + 1) * (boxIdx + 1) }
        return lensSum.sum()
    }
}

private fun String.hash(): Int {
    var hash = 0
    for (c in toCharArray()) {
        hash += c.code
        hash *= 17
        hash %= 256
    }
    return hash
}
