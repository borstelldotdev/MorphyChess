package tests

import main.logic.Board
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.measureTime

val depth = 5

fun performanceTestMovePathEnumeration() {
    println("Running performance test (Perft) with depth $depth ply ...")

    val nodes: Int
    val board = Board.startingPosition()
    val time = measureTime {
        nodes = board.perft(depth)
    }

    val nodesPerSecond = nodes.toDouble() / time.toDouble(DurationUnit.SECONDS)
    println("nodes/second: ${nodesPerSecond.roundToInt()}")
}