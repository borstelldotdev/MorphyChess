package engine

import board.Board
import evaluation.SimpleEval
import main.evaluation.AbstractEvaluator
import main.logic.Move

class Engine(val evaluator: AbstractEvaluator) {
    companion object {
        val defaultEvaluator = SimpleEval()
    }

    init {
        evaluator.init()
    }

    constructor() : this(defaultEvaluator)

    fun bestMove(position: Board, depthPly: Int): Move {
        var bestEval = Int.MIN_VALUE
        var bestMove = Move.NONE

        for (move in position.generateLegalMoves()) {
            position.pushMove(move)
            val eval = searchEval(position, depthPly)
            if (eval > bestEval) {
                bestEval = eval
                bestMove = move
            }
            position.popMove()
        }

        return bestMove
    }

    fun searchEval(position: Board, depthPly: Int): Int {
        return evaluator.relativeEval(position)
    }
}