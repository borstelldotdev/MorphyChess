package main

import main.gui.Gui
import main.tests.runTests
import tests.performanceTestMovePathEnumeration
import uci.UCIInfo
import uci.UCILoop

const val VERSION = "1.0"

fun main(args: Array<String>) {
    println()
    println("MorphyChess $VERSION")
    println("https://github.com/borstelldotdev/MorphyChess")
    println("Usage: java -jar <MorphyChess jar> [mode: (G)ui | (u)ci | (t)est | (p)erformance-test]")
    val mode: String
    if (args.isEmpty()) {
        println("No mode specified, please enter a mode")
        println("(G)ui | (u)ci | (t)est | (p)erformance-test: ")
        val userInput: String = readln().lowercase()

        mode = if (userInput.isEmpty()) {
            "gui"
        } else {
            userInput.split(" ")[0]
        }

    } else {
        mode = args[0]
    }

    when {
        mode.startsWith("g") -> mainVisual()
        mode.startsWith("u") -> mainUCI()
        mode.startsWith("t") -> mainTests()
        mode.startsWith("p") -> performanceTestMovePathEnumeration()
    }
}

fun mainUCI() {
    UCIInfo()
    UCILoop()
}

fun mainVisual() {
    println("Launching MorphyChess in GUI mode...")
    val gui = Gui()
}

fun mainTests () {
    // Yes, this is a stupid way to do testing
    // However, I really can't get normal tests to compile properly
    // So I'm resorting to this to still be able to test my code

    println("Executing tests...")
    runTests()
}
