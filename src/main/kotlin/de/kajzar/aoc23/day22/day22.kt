package de.kajzar.aoc23.day22

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day19.intersect
import java.util.*
import kotlin.math.max
import kotlin.math.min

fun main() {
    val bricks = Day(22)
        .input()
        .readLines()
        .mapIndexed { index, s ->
            val (a, b) = s.split("~")
            Brick(a.toPosition(), b.toPosition())
        }
        .settled()

    val mapping = bricks.foundationMapping()

    bricks.count { b -> diffWhenRemoving(b, mapping) == 0 }
        .also { println("Part 1 result: $it") }

    bricks.sumOf { b -> diffWhenRemoving(b, mapping) }
        .also { println("Part 2 result: $it") }
}

data class Position(val x: Int, val y: Int, val z: Int)

fun Position.moveDown(by: Int): Position = copy(z = z - by)

typealias Brick = Area

fun Brick.below(): Area {
    val bottom = z().first
    return Area(
        Position(x().first, y().first, bottom - 1),
        Position(x().last, y().last, bottom - 1),
    )
}

private fun String.toPosition(): Position {
    val (x, y, z) = split(",").map { it.toInt() }
    return Position(x, y, z)
}

data class Area(val a: Position, val b: Position)

fun Area.x(): IntRange = min(this.a.x, this.b.x)..max(this.a.x, this.b.x)
fun Area.y(): IntRange = min(this.a.y, this.b.y)..max(this.a.y, this.b.y)
fun Area.z(): IntRange = min(this.a.z, this.b.z)..max(this.a.z, this.b.z)

fun Area.moveDown(by: Int): Area {
    return copy(a = a.moveDown(by), b = b.moveDown(by))
}

fun Area.intersect(other: Area): Boolean {
    return !this.x().intersect(other.x()).isEmpty() &&
            !this.y().intersect(other.y()).isEmpty() &&
            !this.z().intersect(other.z()).isEmpty()
}

private val FLOOR = Brick(Position(0, 0, 0), Position(0, 0, 0))

private fun List<Brick>.foundationMapping(): Map<Brick, Set<Brick>> {
    return associate { b ->
        if (b.z().first == 1)
            return@associate b to setOf(FLOOR)

        val below = b.below()
        val foundations = filter { it.intersect(below) }
            .map { it }
            .toSet()

        b to foundations
    }
}

private fun diffWhenRemoving(b: Brick, foundationMapping: Map<Brick, Set<Brick>>): Int {

    val map = foundationMapping.mapValues { (_, v) -> v.toMutableSet() }.toMutableMap()

    val queue: Queue<Brick> = LinkedList<Brick>()
        .also { it.add(b) }

    val lostFoundations = mutableSetOf<Brick>()

    while (queue.isNotEmpty()) {
        val id = queue.poll()

        for ((_, v) in map) {
            v.remove(id)
        }

        map.filter { (_, v) -> v.isEmpty() }.keys.forEach { k ->
            lostFoundations.add(k)
            queue.add(k)
            map.remove(k)
        }
    }

    return lostFoundations.count()
}

fun List<Brick>.settled(): List<Brick> {
    val settled = mutableListOf<Brick>()

    val remaining: Queue<Brick> = LinkedList<Brick>().also { it -> it.addAll(sortedBy { it.z().first }) }

    while (remaining.isNotEmpty()) {
        val b = remaining.poll()
        val nextHeight = settled.heightAt(b.x(), b.y()) + 1
        val diff = b.z().first - nextHeight
        settled.add(b.moveDown(diff))
    }

    return settled
}

fun List<Brick>.heightAt(x: IntRange, y: IntRange): Int {
    return filter { !it.x().intersect(x).isEmpty() && !it.y().intersect(y).isEmpty() }
        .maxOfOrNull { it.z().last } ?: 0
}