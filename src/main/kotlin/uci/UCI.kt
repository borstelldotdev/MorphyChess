package uci

import board.Board
import engine.Engine
import main.gui.Gui
import javax.swing.WindowConstants

@Volatile
var isRunning = false

var game: Board? = null
var engine: Engine? = null

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

fun makeMove(moveString: String) {
    if (game == null) {
        println("Please set up a position first!")
        return
    }

    for (move in game!!.generateLegalMoves()) {
        if (move.toString() == moveString) {
            game!!.pushMove(move)
            return
        }
    }

    println("No such move found!")
}

fun processCommand(line: String) {
    when {
        // Standard UCI commands

        line.startsWith("uci") -> UCIInfo()

        line.startsWith("isready") -> println("readyok")

        line.startsWith("newgame") || line.startsWith("ucinewgame") -> newGame()

        line.startsWith("position") -> {
            val segments = line.trim().split(' ')
            val stream = ArrayDeque<String>()
            stream.addAll(segments)
            stream.removeFirst() // Get rid of `position`

            when (stream.removeFirst()) {
                "fen" -> {
                    var fen = ""
                    for (i in 0..6) {
                        fen += " " + stream.removeFirst()
                    }

                    game = Board.fromFen(fen.trim())
                }

                "startpos" -> {
                    game = Board.startingPosition()
                }

                else -> { println("Invalid command"); return }
            }

            while (stream.isNotEmpty()) {
                val moveString = stream.removeFirst()
                makeMove(moveString)
            }
        }

        line.startsWith("go depth") -> {
            if (game == null) {
                println("Please set up a position first!")
                return
            }
            val depth = line.removePrefix("go depth").trim().toInt()
            val (bestMove, bestEval) = engine!!.bestMove(game!!, depth)
            println("info score cp $bestEval")
            println("bestmove $bestMove")
        }



        // Nonstandard commands

        line.startsWith("list legalmoves") -> {
            println(game?.generateLegalMoves())
        }

        line.startsWith("list pseudolegalmoves") -> {
            println(game?.generatePseudoLegalMoves())
        }

        line.startsWith("info board") -> {
            if (game == null) {
                println("Please set up a position first!")
                return
            }

            println(game)
        }

        line.startsWith("info boardmeta") -> {
            if (game == null) {
                println("Please set up a position first!")
                return
            }

            println(game!!.meta)
        }

        line.startsWith("info boarddata") -> {
            if (game == null) {
                println("Please set up a position first!")
                return
            }

            println(game!!.data)
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
            makeMove(line.removePrefix("play").trim())
        }

        line.startsWith("gui") -> {
            if (game == null) {
                println("Please set up a position first!")
                return
            }

            val gui = Gui(game!!)
            gui.frame.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        }
    }
}

fun UCILoop() {
    val reader = System.`in`.bufferedReader() // To handle EOF properly
    engine = Engine()
    while (true) {
        val line = reader.readLine()?.trim() ?: break
        if (line.startsWith("quit")) break

        processCommand(line)
    }
}