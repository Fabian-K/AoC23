package de.kajzar.aoc23.day24

import de.kajzar.aoc23.Day

fun main() {

    val hailstones = Day(24)
        .input()
        .readLines()
        .map { l -> l.toHailstone() }

    // Part 1
    val range = 200000000000000.0..400000000000000.0
    hailstones.pairs().count { (a, b) ->
        val x = intersection(a.toFunction(), b.toFunction())
        val y = a.toFunction().calculate(x)

        a.isInFutureX(x)
                && b.isInFutureX(x)
                && x in range
                && y in range
    }
        .also { println("Part 1 result: $it") }

    // Part 2
    val eqs = buildEquations {
        val rx = variable("rx") // x pos of rock
        val ry = variable("ry") // y pos of rock
        val rz = variable("rz") // z pos of rock

        val dx = variable("dx") // x velocity of rock
        val dy = variable("dy") // y velocity of rock
        val dz = variable("dz") // z velocity of rock

        for ((idx, hailstone) in hailstones.take(3).withIndex()) {
            val ti = variable("t$idx") // time rock collides with hailstone
            equation("${hailstone.pos.x} + ($ti * ${hailstone.vel.x}) == $rx + ($dx * $ti)")
            equation("${hailstone.pos.y} + ($ti * ${hailstone.vel.y}) == $ry + ($dy * $ti)")
            equation("${hailstone.pos.z} + ($ti * ${hailstone.vel.z}) == $rz + ($dz * $ti)")
        }
    }

    println("Solving equations:")
    println("-----------------------------------------------")
    eqs.dump()
    println()

    println("Example via sage https://sagecell.sagemath.org/")
    println("-----------------------------------------------")
    eqs.dumpSage()
    println()

    // result:
    val rock = "200027938836082, 127127087242193, 219339468239370 @  133, 278, 85".toHailstone()

    listOf(rock.pos.x, rock.pos.y, rock.pos.z)
        .sum()
        .also { println("Part 2 result: ${"%.0f".format(it)}") }
}

private fun String.toHailstone(): Hailstone {
    val (pos, dv) = split(" @ ")
    return Hailstone(pos.toVector(), dv.toVector())
}

private fun String.toVector(): Vector {
    val (x, y, z) = split(", ").map { it.trim().toDouble() }
    return Vector(x, y, z)
}

data class Vector(val x: Double, val y: Double, val z: Double)
data class Hailstone(val pos: Vector, val vel: Vector)

// f(x) = ax + b
data class LinearFunction(val a: Double, val b: Double)

private fun Hailstone.toFunction(): LinearFunction {
    val a = vel.y / vel.x
    val b = pos.y - pos.x * a
    return LinearFunction(a, b)
}

private fun List<Hailstone>.pairs(): List<Pair<Hailstone, Hailstone>> {
    val pairs = mutableListOf<Pair<Hailstone, Hailstone>>()

    for (i in indices) {
        for (j in i + 1 until size) {
            pairs.add(this[i] to this[j])
        }
    }

    return pairs
}

private fun LinearFunction.calculate(x: Double): Double = a * x + b

private fun intersection(a: LinearFunction, b: LinearFunction): Double {
    val p1 = a.a - b.a
    val p2 = b.b - a.b
    return p2 / p1
}

private fun Hailstone.isInFutureX(x: Double): Boolean = if (vel.x > 0)
    x > pos.x
else
    x < pos.x

class Equations {
    private val equations = mutableListOf<String>()
    private val variables = mutableSetOf<String>()

    fun equation(eq: String) {
        equations.add(eq)
    }

    fun variable(name: String): String = name.also { variables.add(it) }
    fun dump() {
        for (equation in equations) {
            println("- $equation")
        }
    }

    fun dumpSage() {
        println("var('${variables.joinToString(",")}')")
        println(equations.joinToString(separator = ",", prefix = "equations = [", postfix = "]"))
        println("solution = solve(equations, ${variables.joinToString(separator = ",")}); solution")
    }
}

fun buildEquations(ctx: Equations.() -> Unit): Equations {
    return Equations()
        .apply(ctx)
}