package de.kajzar.aoc23.day20

import de.kajzar.aoc23.Day
import leastCommonMultiplier
import java.util.*

fun main() {
    // part 1
    val config = Day(20)
        .input()
        .readLines()
        .map { it.split(" -> ") }
        .map { (n, d) ->
            val destinations = d.split(", ")

            val (type, name) = when {
                n == "broadcaster" -> "broadcaster" to "broadcaster"
                n.startsWith("%") -> "%" to n.drop(1)
                n.startsWith("&") -> "&" to n.drop(1)
                else -> error("unknown module type: $n")
            }
            Triple(name, type, destinations)
        }

    // part 1
    Counter().apply {
        val setup = newSetup(config)
        repeat(1_000) { process(setup) { _, _, pulse -> onPulse(pulse) } }
        println("Part 1 result: ${countLow * countHigh}")
    }


    // part 2
    val setup = newSetup(config)

    // rx has a single input of type Conjunction
    val rxInputRef = config.filter { it.third.contains("rx") }.map { it.first }.single()
    val rxInputModule = setup[rxInputRef] as Module.Conjunction

    // all inputs are loops -> find size of loops
    LoopSizeDetector(rxInputRef, rxInputModule.inputs).apply {
        var i = 0
        while (!allFound()) {
            i += 1
            process(setup) { source, target, pulse -> onPulse(source, target, pulse, i) }
        }
    }
        .loopSizes()
        .leastCommonMultiplier()
        .also { println("Part 2 result: $it") }
}

typealias ModuleRef = String
typealias Setup = Map<ModuleRef, Module>

enum class Pulse {
    HIGH,
    LOW,
}

sealed class Module(
    private val destinations: List<ModuleRef>,
) {
    abstract fun receive(pulse: Pulse, source: ModuleRef, outbound: (ModuleRef, Pulse) -> Unit)

    fun ((ModuleRef, Pulse) -> Unit).send(pulse: Pulse) {
        for (destination in destinations) {
            this(destination, pulse)
        }
    }

    class FlipFlop(destinations: List<ModuleRef>) : Module(destinations) {
        private var on: Boolean = false
        override fun receive(pulse: Pulse, source: ModuleRef, outbound: (ModuleRef, Pulse) -> Unit) {
            if (pulse == Pulse.LOW) {
                if (!on) {
                    outbound.send(Pulse.HIGH)
                } else {
                    outbound.send(Pulse.LOW)
                }
                on = !on
            }
        }
    }

    class Conjunction(val inputs: List<ModuleRef>, destinations: List<ModuleRef>) : Module(destinations) {
        private var state = inputs.associateWith { Pulse.LOW }.toMutableMap()
        override fun receive(pulse: Pulse, source: ModuleRef, outbound: (ModuleRef, Pulse) -> Unit) {
            state[source] = pulse
            if (state.all { it.value == Pulse.HIGH }) {
                outbound.send(Pulse.LOW)
            } else {
                outbound.send(Pulse.HIGH)
            }
        }
    }

    class Broadcaster(destinations: List<ModuleRef>) : Module(destinations) {
        override fun receive(pulse: Pulse, source: ModuleRef, outbound: (ModuleRef, Pulse) -> Unit) {
            outbound.send(pulse)
        }

    }

    class Dummy : Module(emptyList()) {
        override fun receive(pulse: Pulse, source: ModuleRef, outbound: (ModuleRef, Pulse) -> Unit) {
            // do nothing
        }
    }
}

fun process(setup: Setup, listener: (ModuleRef, ModuleRef, Pulse) -> Unit) {
    val queue: Queue<Triple<ModuleRef, ModuleRef, Pulse>> = LinkedList<Triple<ModuleRef, ModuleRef, Pulse>>().apply {
        push(Triple("root", "broadcaster", Pulse.LOW))
    }

    while (queue.isNotEmpty()) {
        val (source, moduleRef, pulse) = queue.remove()
        listener(source, moduleRef, pulse)
        val module = setup[moduleRef] ?: Module.Dummy()
        module.receive(pulse, source) { m, p ->
            queue.add(Triple(moduleRef, m, p))
        }
    }
}

class Counter {
    var countLow = 0
    var countHigh = 0
    fun onPulse(pulse: Pulse) {
        if (pulse == Pulse.LOW) {
            countLow++
        } else {
            countHigh++
        }
    }
}

class LoopSizeDetector(private val moduleTarget: ModuleRef, modules: List<ModuleRef>) {

    private val loopSizes: MutableMap<ModuleRef, Int?> = modules.associateWith { null }
        .toMutableMap()

    fun allFound() = loopSizes.all { it.value != null }
    fun loopSizes() = loopSizes.values.mapNotNull { it?.toLong() }

    fun onPulse(source: ModuleRef, target: ModuleRef, pulse: Pulse, iteration: Int) {
        if (target == moduleTarget && pulse == Pulse.HIGH) {
            if (loopSizes[source] == null) {
                loopSizes[source] = iteration
            }
        }
    }
}

private fun newSetup(config: List<Triple<String, String, List<String>>>): Map<String, Module> {
    return config.associate { (name, type, destinations) ->
        val module = when (type) {
            "broadcaster" -> Module.Broadcaster(destinations)
            "%" -> Module.FlipFlop(destinations)
            "&" -> {
                val inputs = config.filter { it.third.contains(name) }.map { it.first }
                Module.Conjunction(inputs, destinations)
            }

            else -> error("unknown module type: $name")
        }
        name to module
    }
}
