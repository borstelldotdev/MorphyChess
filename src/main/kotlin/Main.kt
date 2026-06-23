package main

import main.gui.Gui

const val VERSION = "alpha-1.0"

fun main(args: Array<String>) {
    println("MorphyChess $VERSION")
    println("https://github.com/borstelldotdev/MorphyChess")
    println("Usage: java -jar <MorphyChess jar> [mode: (G)ui | (u)ci | (t)est | (m)atch-manager]")
    val mode: String
    if (args.isEmpty()) {
        println("No mode specified, please enter a mode")
        println("(G)ui | (u)ci | (t)est | (m)atch-manager: ")
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
        mode.startsWith("g") -> mainVisual()
    }
}

fun mainUCI() {
    println("id name MorphyChess")
    println("id author borstelldotdev")

    // TODO("add options")

    println("uciok")
}

fun mainVisual() {
    val gui = Gui()
}

fun runTests () {
    // Yes, this is a stupid way to do testing
    // However, I really can't get normal tests to compile properly
    // So I'm resorting to this to still be able to test my code

    println("Executing tests...")
}

fun matchManager() {
    println("Starting match manager...")

    // TODO("Implement match manager")
}