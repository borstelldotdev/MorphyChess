@file:Suppress("SpellCheckingInspection")

package main.logic



@JvmInline
value class BoardData(val value: IntArray) {
    companion object {
        fun fromFen(fen: String): Triple<BoardData, Square, Square> {
            val new = BoardData(IntArray(64))
            var whiteKing: Square = Square(0)
            var blackKing: Square = Square(0)
            val ranks = fen.split('/') // Single quotes: Char
            for (rank in 0..7) {
                var file = 0
                for (ch in ranks[rank].toCharArray()) {
                    if (ch.isDigit()) {
                        file += ch.toString().toInt() // not pretty
                    } else {
                        new[file, rank] = Piece.fromChar(ch).value
                        when (ch) {
                            'K' -> whiteKing = Square.of(file, rank)
                            'k' -> blackKing = Square.of(file, rank)
                        }
                        file += 1
                    }
                }
            }


            return Triple(new, whiteKing, blackKing)
        }
    }

    operator fun get(x: Int, y: Int): Int {
        if (!(x in 0..7 && y in 0..7)) {
            return 0
        }

        return value[x + (y * 8)]
    }

    operator fun get(square: Square): Int {
        return get(square.x, square.y)
    }

    operator fun set(x: Int, y: Int, value: Int) {
        if (!(x in 0..7 && y in 0..7)) {
            return
        }

        this@BoardData.value[x + (y * 8)] = value
    }

    operator fun set(square: Square, value: Int) {
        return set(square.x, square.y, value)
    }

    fun atUnsafe(x: Int, y: Int): Int {
        return value[x + (y * 8)]
    }

    fun at(x: Int, y: Int): Int {
        return get(x, y)
    }

    fun at(square: Square): Int {
        return get(square.x, square.y)
    }
}


class Board(val data: BoardData, val meta: BoardMeta) {
    companion object {
        fun fromFen(fen: String): Board {
            val (boardStr, metaStr) = fen.split(' ', limit = 2)
            val (boardData, whiteKing, blackKing) = BoardData.fromFen(boardStr)
            val boardMeta = BoardMeta.fromFen(metaStr, whiteKing, blackKing)
            return Board(boardData, boardMeta)
        }

        fun startingPosition(): Board {
            return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        }
    }

    fun genPawnMoves(): List<Move> {
        val l = mutableListOf<Move>()
        return l
    }

    fun generatePseudoLegalMoves(): List<Move> {
        val moves = mutableListOf<Move>()
        for (x in 0..7) {
            for (y in 0..7) {
                val piece = Piece(data.atUnsafe(x, y))
                if (piece.isEmpty or (piece.owner != meta.toMove)) continue

                when (piece.pieceValue) {

                }
            }
        }
        return moves
    }

    fun generateLegalMoves(): List<Move> {
        val pseudoLegal = generateLegalMoves()
        return pseudoLegal
    }

    fun perft(depth: Int): Int {
        // PERFormance Test, move path enumeration
        // with bulk counting
        val legalMoves = generateLegalMoves()

        if (depth == 1)
            return legalMoves.size

        var sum = 0
        for (move in legalMoves) {

        }
        return sum
    }


    override fun toString(): String {
        // TODO: fix
        val str = "main.logic.Board\n  A B C D E F G H I\n\n"

        return  str
    }
}