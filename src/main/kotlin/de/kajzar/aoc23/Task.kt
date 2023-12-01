package de.kajzar.aoc23

class Day(private val nr: Int) {
    fun input() =
        this::class.java.getResourceAsStream("/input/$nr.txt")?.bufferedReader() ?: error("Input missing")
}
