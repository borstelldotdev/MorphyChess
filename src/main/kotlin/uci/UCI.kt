package uci

import board.Board

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

fun processCommand(line: String) {
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
            if (game == null) {
                println("Please set up a position first!")
                return
            }

            println(game)
        }

        line.startsWith("go perft") -> {
            if (game == null) {
                println("Please set up a position first!")
                return
            }

            val depth = line.removePrefix("go perft").trim().toInt()
            game!!.perftVerbose(depth)
        }

        line.startsWith("play") -> {
            if (game == null) {
                println("Please set up a position first!")
                return
            }

            val moveString = line.removePrefix("go perft").trim()
            for (move in game!!.generateLegalMoves()) {
                if (move.toString() == moveString) {
                    game!!.pushMove(move)
                }
            }
        }
    }
}

fun UCILoop() {
    val reader = System.`in`.bufferedReader() // To handle EOF properly
    while (true) {
        val line = reader.readLine()?.trim() ?: break

        processCommand(line)
    }
}