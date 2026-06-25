package uci

import main.logic.Board

@Volatile
var isRunning = false

var game: Board? = null

fun UCIInfo() {
    println("id name MorphyChess")
    println("id author borstelldotdev")

    // TODO("add options")

    println("uciok")
}

fun newGame() {
    game = Board.startingPosition()

    // TODO("reset transposition table")
}

fun UCILoop() {
    val reader = System.`in`.bufferedReader() // To handle EOF properly
    while (true) {
        val line = reader.readLine()?.trim() ?: break
        when {
            // Standard UCI commands

            line.startsWith("uci") -> UCIInfo()

            line.startsWith("isready") -> println("readyok")

            line.startsWith("newgame") -> newGame()

            line.startsWith("position startpos") -> newGame()

            line.startsWith("position fen") -> {
                val fen = line.removePrefix("position fen").trim()
                game = Board.fromFen(fen)
            }

            // Nonstandard commands
            line.startsWith("list legalmoves") -> {
                println(game?.generateLegalMoves())
            }

            line.startsWith("list pseudolegalmoves") -> {
                println(game?.generatePseudoLegalMoves())
            }

            line.startsWith("info boardmeta") -> {
                println("To move: ${game.meta.toMove}")
            }
        }
    }
}