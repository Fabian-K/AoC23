package de.kajzar.aoc23.day6

import de.kajzar.aoc23.Day

fun main() {

    val raceList = Day(6)
        .input()
        .lines().toList()
        .map { it.substringAfter(":") }
        .map { line ->
            line.split(" ")
                .filter { it.isNotBlank() }
                .map { it.toLong() }
        }
        .let { (times, distances) ->

            times.indices.map { i ->
                Race(times[i], distances[i])
            }
        }

    raceList.map { it.optionsToBeatRecord() }
        .reduce { a, b -> a * b }
        .let { println("Part 1 Result: $it") }

    // part 2
    val racePart2 = Day(6)
        .input()
        .lines().toList()
        .map { it.substringAfter(":") }
        .map { it.filter { c -> c.isDigit() }.toLong() }
        .let { (time, distance) -> Race(time, distance) }

    println("Part 2 Result: ${racePart2.optionsToBeatRecord()}")
}

data class Race(
    val time: Long,
    val recordDistance: Long,
)

fun Race.optionsToBeatRecord(): Int {
    return (1..time).count { holdDown ->
        val timeToMove = time - holdDown
        val distanceTraveled = holdDown * timeToMove
        distanceTraveled > recordDistance
    }
}