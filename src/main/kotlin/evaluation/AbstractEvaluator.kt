package main.evaluation

import main.logic.Board
import main.logic.Player

interface AbstractEvaluator {
    fun init()

    fun eval(position: Board): Int

    fun relativeEval(position: Board): Int {
        return when (position.meta.toMove) {
            Player.WHITE -> eval(position)
            Player.BLACK -> -eval(position)
            else -> 0
        }
    }
}