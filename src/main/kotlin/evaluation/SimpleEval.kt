package evaluation

import main.evaluation.AbstractEvaluator
import board.Board
import main.logic.Square


class SimpleEval : AbstractEvaluator {
    override fun init() {
        println("info string using evaluation function SimpleEval")
    }

    override fun eval(position: Board): Int {
        var accumulator = 0

        for (pos in 0..63) {
            val square = Square(pos)
            val piece = position.data.atUnsafe(square)
        }

        return accumulator
    }

}