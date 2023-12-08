import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day5.split

data class Input(
    val directions: Sequence<Char>,
    val mapping: Map<String, Pair<String, String>>,
)

fun main() {

    val (directions, mapping) = Day(8)
        .input()
        .lines().toList()
        .split { it.isBlank() }
        .let { (directionString, mappingString) ->
            val seq = sequence {
                while (true) {
                    directionString.single().toCharArray().toList().forEach {
                        yield(it)
                    }
                }
            }

            val mapping = mappingString.associate {
                val node = it.substringBefore(" =")
                val (left, right) = it.substringAfter("(").substringBefore(")")
                    .split(", ")
                node to (left to right)
            }
            Input(seq, mapping)
        }

    fun walker(start: String) = sequence {
        var location = start

        var steps = 0L
        directions.forEach { d ->
            val next = when (d) {
                'L' -> mapping[location]!!.first
                'R' -> mapping[location]!!.second
                else -> error("")
            }
            steps += 1
            location = next
            yield(next to steps)
        }
    }

    // part 1
    walker("AAA")
        .first { (loc, _) -> loc == "ZZZ" }
        .let { (_, steps) -> println("Part 1 result: $steps") }

    // part 2

    // each starting position is part of a loop -> find loop size
    val loopSizes = mapping.keys.filter { it.endsWith("A") }
        .map {
            val (_, steps) = walker(it)
                .first { (pos, _) -> pos.endsWith("Z") }
            steps
        }

    loopSizes.leastCommonMultiplier()
        .let { println("Part 2 result: $it") }
}

fun List<Long>.leastCommonMultiplier() = reduce { a, b -> leastCommonMultiplier(a, b) }

fun leastCommonMultiplier(a: Long, b: Long): Long {
    val larger = if (a > b) a else b
    val maxLcm = a * b
    var lcm = larger
    while (lcm <= maxLcm) {
        if (lcm % a == 0L && lcm % b == 0L) {
            return lcm
        }
        lcm += larger
    }
    return maxLcm
}
