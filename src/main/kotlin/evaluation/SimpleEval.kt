package evaluation

import main.evaluation.AbstractEvaluator
import board.Board
import board.Player
import main.logic.PieceType
import main.logic.Square


class SimpleEval : AbstractEvaluator {
    override fun init() {
        println("info string using evaluation function SimpleEval")
    }

    override fun eval(position: Board): Int {
        var middlegameEval = 0
        var endgameEval = 0
        var endgameScore = 0

        for (pos in 0..63) {
            val square = Square(pos)
            val piece = position.data.atUnsafe(square)
            val owner = piece.owner

            when (piece.pieceValue) {
                PieceType.PAWN.value -> {
                    middlegameEval  += pawnPSq.query(square, owner, GameState.MIDDLEGAME)
                    endgameEval     += pawnPSq.query(square, owner, GameState.ENDGAME)
                }

                PieceType.KNIGHT.value -> {
                    middlegameEval  += knightPSq.query(square, owner, GameState.MIDDLEGAME)
                    endgameEval     += knightPSq.query(square, owner, GameState.ENDGAME)
                    endgameScore += 1
                }

                PieceType.BISHOP.value -> {
                    middlegameEval  += bishopPSq.query(square, owner, GameState.MIDDLEGAME)
                    endgameEval     += bishopPSq.query(square, owner, GameState.ENDGAME)
                    endgameScore += 1
                }

                PieceType.ROOK.value -> {
                    middlegameEval  += rookPSq.query(square, owner, GameState.MIDDLEGAME)
                    endgameEval     += rookPSq.query(square, owner, GameState.ENDGAME)
                    endgameScore += 2
                }

                PieceType.QUEEN.value -> {
                    middlegameEval  += queenPSq.query(square, owner, GameState.MIDDLEGAME)
                    endgameEval     += queenPSq.query(square, owner, GameState.ENDGAME)
                    endgameScore += 4
                }

                PieceType.KING.value -> {
                    middlegameEval  += kingPSq.query(square, owner, GameState.MIDDLEGAME)
                    endgameEval     += kingPSq.query(square, owner, GameState.ENDGAME)
                }
            }
        }

        val endgameWeight = endgameScore.toDouble() / 24
        val finalEval = ((endgameEval * endgameWeight) + (middlegameEval * (1 - endgameWeight))).toInt()
        return finalEval
    }
}