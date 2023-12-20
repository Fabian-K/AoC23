package de.kajzar.aoc23.day19

import de.kajzar.aoc23.Day
import de.kajzar.aoc23.day5.split
import kotlin.math.max

fun main() {
    val (parts, workflows) = parseInput()

    // part 1
    val acceptedRanges = optionsLeadingTo(workflows, "A")
        .map { mergeByAttribute(it) }

    parts.filter { p -> acceptedRanges.any { it.matchesPart(p) } }
        .sumOf { it.value() }
        .also { println("Part 1 result: $it") }

    // part 2
    acceptedRanges
        .sumOf { it.values.toCombinations() }
        .also { println("Part 2 result: %.0f".format(it)) }
}

typealias Part = Map<String, Int>

data class Rule(val condition: Condition?, val target: String)
data class Condition(val attribute: String, val comparator: String, val value: Int)

typealias Workflow = List<Rule>

typealias Workflows = Map<String, Workflow>

private fun Part.value() = values.sum()

private fun Map<String, IntRange>.matchesPart(p: Part): Boolean =
    p.entries.all { (attribute, value) -> getValue(attribute).contains(value) }

data class AttributeRange(val attribute: String, val range: IntRange)

private fun mergeByAttribute(ranges: List<AttributeRange>): Map<String, IntRange> {
    return listOf("x", "m", "a", "s").associateWith { attribute ->
        ranges.filter { it.attribute == attribute }
            .fold(1..4000) { acc, range ->
                acc.intersect(range.range)
            }
    }
}

fun IntRange.intersect(other: IntRange): IntRange {
    val start = max(this.first, other.first)
    val end = minOf(this.last, other.last)
    return start..end
}

private fun Collection<IntRange>.toCombinations(): Double {
    return this.fold(1.0) { acc, range ->
        acc * range.count()
    }
}

private fun optionsLeadingTo(
    workflows: Workflows,
    target: String,
): List<List<AttributeRange>> {

    val options = mutableListOf<List<AttributeRange>>()

    for ((name, workflow) in workflows) {
        for ((ruleIdx, rule) in workflow.withIndex()) {
            if (rule.target == target) {
                val ranges = mutableListOf<AttributeRange>()

                if (rule.condition != null)
                    ranges.add(AttributeRange(rule.condition.attribute, rule.condition.toRangeForInclusion()))

                workflow.take(ruleIdx)
                    .mapNotNull { it.condition }
                    .map { AttributeRange(it.attribute, it.toRangeForExclusion()) }
                    .forEach { ranges.add(it) }

                optionsLeadingTo(workflows, name)
                    .ifEmpty { listOf(emptyList()) }
                    .map { it + ranges }
                    .forEach { options.add(it) }
            }
        }
    }

    return options
}

private fun Condition.toRangeForExclusion(): IntRange = when (comparator) {
    "<" -> value..4_000
    else -> 1..value
}

private fun Condition.toRangeForInclusion(): IntRange = when (comparator) {
    "<" -> 1 until value
    else -> value + 1..Int.MAX_VALUE
}

private fun parseInput(): Pair<List<Part>, Workflows> {
    val (workflowInput, partInput) = Day(19)
        .input()
        .readLines()
        .split { it.isBlank() }

    val parts = partInput.map { line ->
        line.removePrefix("{").removeSuffix("}")
            .split(",")
            .map { it.split("=") }
            .associate { (key, value) -> key to value.toInt() }
    }

    val workflows: Workflows = workflowInput
        .associate { line ->
            val name = line.substringBefore("{")
            val rules = line.substringAfter("{").substringBefore("}")
                .split(",")
                .map { r ->
                    val condition = (if (r.contains(":")) r.substringBefore(":") else null)?.let {
                        if (it.contains("<")) {
                            val (attribute, value) = it.split("<")
                            Condition(attribute, "<", value.toInt())
                        } else {
                            val (attribute, value) = it.split(">")
                            Condition(attribute, ">", value.toInt())
                        }
                    }
                    val target = if (condition == null) r else r.substringAfter(":")
                    Rule(condition, target)
                }

            name to rules
        }
    return parts to workflows
}