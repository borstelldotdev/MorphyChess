package evaluation

import main.evaluation.AbstractEvaluator
import main.logic.Board
import main.logic.Square


class SimpleEval : AbstractEvaluator {
    override fun init() {
        TODO("Not yet implemented")
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