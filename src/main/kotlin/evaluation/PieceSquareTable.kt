package main.evaluation

import main.logic.Player
import main.logic.Square

enum class GameState(val value: Int) {
    MIDDLEGAME(2),
    ENDGAME(1)
}

@JvmInline
value class PieceSquareTable(val value: Array<IntArray>) {
    // A simple class for querying piece-square
    // Using value classes should produce minimal overhead
    //
    // Is internally stored as two nested arrays
    // The inner one represents a piece-square-table
    // The outer array stores the four different piece-square-table for each piece
    //
    // Table assignments:
    // T0: White middlegame table
    // T1: White endgame table
    // T2: Black middlegame table
    // T3: Black endgame table

    fun query(square: Square, player: Player, gameState: GameState): Int {
        return value[(player.value shr 3) - gameState.value][square.value]
    }

    fun query(x: Int, y: Int, player: Player, gameState: GameState): Int {
        return value[(player.value shr 3) - gameState.value][x + (y shl 3)]
    }

    companion object {
        fun cloneTable(table: IntArray, staticWeight: Int, flip: Boolean): IntArray {
            val newTable
        }

        fun createTable(middlegameTable: IntArray, endgameTable: IntArray, staticWeight: Int): PieceSquareTable {
            val
        }
    }
}