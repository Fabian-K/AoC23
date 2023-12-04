package de.kajzar.aoc23.day4

import de.kajzar.aoc23.Day
import kotlin.math.pow

fun main() {

    val cards: List<Card> =
        Day(4).input()
            .lines().toList()
            .map { line ->
                val numberSequences = line.substringAfter(":").split("|")
                val numberLists = numberSequences.map { seq ->
                    seq.split(" ")
                        .filter { it.isNotBlank() }
                        .map { it.toInt() }
                }
                (numberLists[0] to numberLists[1])
            }

    // part 1
    cards.sumOf { it.score() }
        .let { println("Part 1 Sum: $it") }

    // part 2
    val cardCounts = cards.indices.associateWith { 1 }.toMutableMap()

    (0 until cards.count()).forEach { idx ->
        val card = cards[idx]
        val matchingNumbers = card.matchingNumbers().count()

        val cardCount = cardCounts[idx]!!
        val affectedCardIdxs = (idx + 1) until (idx + 1) + matchingNumbers
        affectedCardIdxs.forEach { affectedIdx ->
            cardCounts[affectedIdx] = cardCounts[affectedIdx]!! + cardCount
        }
    }

    cardCounts.values.sum()
        .let { println("Part 2 Sum: $it") }
}

typealias Card = Pair<List<Int>, List<Int>>

fun Card.winningNumbers() = first
fun Card.ownNumbers() = second
fun Card.matchingNumbers(): List<Int> {
    return ownNumbers()
        .filter { it in winningNumbers() }
}

fun Card.score(): Double {
    val matching = matchingNumbers()

    return when (matching.count()) {
        0 -> 0.0
        else -> 2.toDouble().pow(matching.count() - 1)
    }
}