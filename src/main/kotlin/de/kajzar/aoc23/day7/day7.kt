package de.kajzar.aoc23.day7

import de.kajzar.aoc23.Day
import kotlin.streams.toList

fun main() {

    // parse input
    val hands = Day(7)
        .input()
        .lines().toList()
        .map { it.split(" ") }
        .map { (cardString, bidString) ->
            val bid = bidString.toInt()
            val hand: Hand = cardString.chars().toList().map { it.toChar() }
            hand to bid
        }

    // part 1
    hands
        .sortedWith(
            compareBy(byType, selector = { it: Pair<Hand, Int> -> it.first.type() })
                .thenBy(handByRanking(cardRanking), selector = { it.first })
                .reversed()
        )
        .mapIndexed { index, (_, bid) -> bid to index + 1 }
        .sumOf { (a, b) -> a * b }
        .let { println("Part 1 result: $it") }

    // part 2
    hands
        .map { (hand, bid) -> Triple(hand, hand.replaceJoker(), bid) }
        .sortedWith(
            compareBy(byType, selector = { (_, replaced, _): Triple<Hand, Hand, Int> -> replaced.type() })
                .thenBy(handByRanking(alternativeCardRanking), selector = { (old, _, _) -> old })
                .reversed()
        )
        .mapIndexed { index, (_, _, bid) -> bid to index + 1 }
        .sumOf { (a, b) -> a * b }
        .let { println("Part 2 result: $it") }
}

typealias Card = Char
typealias Hand = List<Card>

private val cardRank = listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')
private val adjustedCardRank = cardRank.filter { it != 'J' }.plus('J')

val cardRanking = compareBy<Card> { cardRank.indexOf(it) }
val alternativeCardRanking = compareBy<Card> { adjustedCardRank.indexOf(it) }

fun handByRanking(cardRanking: Comparator<Card>) = object : Comparator<Hand> {
    override fun compare(a: List<Card>, b: List<Card>): Int {
        for (i in a.indices) {
            val r = cardRanking.compare(a[i], b[i])
            if (r != 0) return r
        }
        return 0
    }
}

val byType = compareBy<Type> { it.rank() }

enum class Type {
    FiveOfAKind,
    FourOfAKind,
    FullHouse,
    ThreeOfAKind,
    TwoPair,
    OnePair,
    HighCard,
}

fun Type.rank() = Type.entries.indexOf(this)

fun Hand.cardCount() = groupBy { it }
    .mapValues { (_, items) -> items.count() }
    .toList()

fun Hand.replaceJoker(): Hand {
    if (none { it == 'J' }) return this

    val cardCount = cardCount()

    val highestNonJoker = cardCount
        .filter { (k, _) -> k != 'J' }
        .maxOfOrNull { (_, v) -> v }

    val bestChoice = cardCount
        .filter { (_, v) -> v == highestNonJoker }
        .maxOfWithOrNull(alternativeCardRanking.reversed(), selector = { (k, _) -> k })
        ?: 'A'

    return map { c ->
        if (c == 'J') bestChoice
        else c
    }
}

fun Hand.type(): Type {
    val sortedCardCount = cardCount()
        .sortedByDescending { (_, v) -> v }

    return when {
        sortedCardCount.size == 1 -> Type.FiveOfAKind
        sortedCardCount.size == 2 && sortedCardCount.first().second == 4 -> Type.FourOfAKind
        sortedCardCount.size == 2 && sortedCardCount.first().second == 3 -> Type.FullHouse
        sortedCardCount.first().second == 3 -> Type.ThreeOfAKind
        sortedCardCount[1].second == 2 -> Type.TwoPair
        sortedCardCount.first().second == 2 -> Type.OnePair
        else -> Type.HighCard
    }
}