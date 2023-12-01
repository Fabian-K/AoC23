package de.kajzar.aoc23.day1

import de.kajzar.aoc23.Day


fun main() {

    val digits = (0..9).map { it.digitToChar() }

    Day(1)
        .input()
        .lineSequence()
        .map { it ->
            val first = it.toCharArray().first { it in digits }
            val last = it.toCharArray().last { it in digits }
            "$first$last".toInt()
        }
        .sum()
        .let { println("Part 1 Sum: $it") }

    val stringMapping = mapOf(
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9,
    )

    val digitMapping = (0..9).associateBy { it.toString() }

    data class Match(val position: Int, val digit: Int)

    fun findStart(values: Map<String, Int>, input: String): Match? {
        val firstMatch = values
            .filter { (k, _) -> input.contains(k) }
            .minByOrNull { (k, _) -> input.substringBefore(k).count() }
            ?: return null
        val pos = input.substringBefore(firstMatch.key).count()
        return Match(pos, firstMatch.value)
    }

    fun findEnd(values: Map<String, Int>, input: String): Match? {
        val firstMatch = values
            .filter { (k, _) -> input.contains(k) }
            .minByOrNull { (k, _) -> input.substringAfterLast(k).count() }
            ?: return null
        val pos = input.substringAfterLast(firstMatch.key).count()
        return Match(pos, firstMatch.value)
    }

    Day(1)
        .input()
        .lineSequence()
        .map { line ->
            val (_, first) = listOfNotNull(
                findStart(stringMapping, line),
                findStart(digitMapping, line),
            ).minBy { it.position }

            val (_, second) = listOfNotNull(
                findEnd(stringMapping, line),
                findEnd(digitMapping, line),
            ).minBy { it.position }

            "$first$second".toInt()
        }
        .sum()
        .let { println("Part 2 Sum: $it") }
}