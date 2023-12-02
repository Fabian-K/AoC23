import de.kajzar.aoc23.Day

data class Draw(val content: Map<String, Int>) {
    operator fun get(color: String): Int = content[color] ?: 0
}

data class Game(
    val id: Int,
    val draws: List<Draw>,
) {
    fun isPossible(bag: Map<String, Int>): Boolean {
        return bag.all { (color, bagCount) ->
            draws.all { draw ->
                draw[color] <= bagCount
            }
        }
    }

    fun minimalBag(): Map<String, Int> {
        val colors = draws.flatMap { it.content.keys }.distinct()
        return colors.associateWith { color -> draws.maxOf { it[color] } }
    }
}

fun Map<String, Int>.power() = values.reduce { a, i -> a * i }

fun main() {
    val games = Day(2).input()
        .lines()
        .map { line ->
            val id = line.substringBefore(":").substringAfter(" ").toInt()
            val draws = line.substringAfter(":")
                .split(";")
                .map { drawContentInput ->
                    val draws = drawContentInput.split(",").map { it.trim() }
                        .associate {
                            val parts = it.split(" ")
                            parts[1] to parts[0].toInt()
                        }
                    Draw(draws)
                }
            Game(id, draws)
        }
        .toList()

    games.filter {
        it.isPossible(
            mapOf(
                "red" to 12,
                "green" to 13,
                "blue" to 14,
            )
        )
    }
        .sumOf { it.id }
        .let { println("Part 1 Sum: $it") }

    games.sumOf { it.minimalBag().power() }
        .let { println("Part 2 Sum: $it") }
}