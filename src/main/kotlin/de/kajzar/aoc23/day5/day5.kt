package de.kajzar.aoc23.day5

import de.kajzar.aoc23.Day
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() = runBlocking(Dispatchers.Default) {

    val inputParts =
        Day(5)
            .input()
            .lines().toList()
            .split { it.isBlank() }

    // pars parts
    val seeds = inputParts.first().single().substringAfter(": ").split(" ").map { it.toLong() }

    val rawMappings: RawMappings = inputParts.drop(1).associate { part ->
        // parse map
        val name = part.first().substringBefore(" map:")
        val numbers = part.drop(1).map { lines -> lines.split(" ").map { it.toLong() } }
        name to numbers
    }

    val mappings = rawMappings.mapValues { (_, v) ->
        val mappings = v
            .map { numbers ->
                val destStart = numbers.first()
                val sourceStart = numbers[1]
                val length = numbers[2]
                (sourceStart until (sourceStart + length)) to (destStart until (destStart + length))
            }
        mappings
    }

    val mappingSequence = listOf(
        "seed-to-soil",
        "soil-to-fertilizer",
        "fertilizer-to-water",
        "water-to-light",
        "light-to-temperature",
        "temperature-to-humidity",
        "humidity-to-location",
    )

    val mergedFullMapping = mappingSequence.fold(listOf<RangeMapping>().asResolver()) { v, nextKey ->
        merge(v, mappings[nextKey]!!.asResolver())
    }

    seeds.minOf { mergedFullMapping[it] }
        .let { println("Part 1 Result: $it") }

    // second part
    val jobs = inputParts.first().single().substringAfter(": ").split(" ").map { it.toLong() }.chunked(2)
        .map { (a, b) -> a until a + b }
        .map { r ->
            async {
                var min = Long.MAX_VALUE
                r.forEach {
                    val loc = mergedFullMapping[it]
                    if (loc < min) {
                        min = loc
                    }
                }
                min
            }
        }

    jobs.minOfOrNull { it.await() }
        .let { println("Part 2 Result: $it") }
}

typealias RawMappings = Map<String, List<List<Long>>>

typealias RangeMapping = Pair<LongRange, LongRange>

fun RangeMapping.source() = first
fun RangeMapping.target() = second

interface Resolver {
    fun affects(value: Long): Boolean
    operator fun get(value: Long): Long
}

typealias Mapping = List<RangeMapping>

fun Mapping.asResolver() = object : Resolver {
    override fun affects(value: Long): Boolean {
        return any { value in it.source() }
    }

    override fun get(value: Long): Long {
        val matchingMapping = firstOrNull { value in it.source() }
        if (matchingMapping != null) {
            val idx = value - matchingMapping.source().first
            return matchingMapping.target().first + idx
        }

        return value
    }

}

fun merge(a: Resolver, b: Resolver): Resolver {

    return object : Resolver {

        override fun affects(value: Long): Boolean {
            return a.affects(value) || b.affects(value)
        }

        override fun get(value: Long): Long {
            if (a.affects(value)) {
                val valueAfterA = a[value]
                if (b.affects(valueAfterA)) {
                    return b[valueAfterA]
                }
                return valueAfterA
            }
            if (b.affects(value)) {
                return b[value]
            }
            return value
        }
    }
}

fun List<String>.split(pred: (String) -> Boolean): List<List<String>> {
    val result = mutableListOf<List<String>>()
    var next = mutableListOf<String>()
    forEach { l ->
        if (pred(l)) {
            result.add(next.toList())
            next = mutableListOf()
        } else {
            next.add(l)
        }
    }
    result.add(next)
    return result
}