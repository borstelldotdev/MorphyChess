package main.logic

@JvmInline
value class BoardData(val value: IntArray) {
    companion object {
        fun fromFen(fen: String): Triple<BoardData, Square, Square> {
            val new = BoardData(IntArray(64))
            var whiteKing = Square(0)
            var blackKing = Square(0)
            val ranks = fen.split('/') // Single quotes: Char
            for (rank in 0..7) {
                var file = 0
                for (ch in ranks[rank].toCharArray()) {
                    if (ch.isDigit()) {
                        file += ch.toString().toInt() // not pretty
                    } else {
                        new[file, rank] = Piece.fromChar(ch)
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

    override fun toString(): String {
        var str = "  a   b   c   d   e   f   g   h\n"
        for (y in 0..7) {
            str += "+---+---+---+---+---+---+---+---+\n"
            for (x in 0..7) {
                str += "| ${get(x, y).toChar()} "
            }
            str += "| ${8 - y}\n"
        }
        str += "+---+---+---+---+---+---+---+---+\n"
        str += "  a    b   c   d   e   f   g   h\n"

        return str
    }

    operator fun get(x: Int, y: Int): Piece {
        if (!(x in 0..7 && y in 0..7)) {
            return Piece.EMPTY
        }

        return Piece(value[x + (y shl 3)])
    }

    operator fun get(square: Square): Piece {
        if (!square.isValid)
            return Piece.EMPTY

        return Piece(value[square.value])
    }

    operator fun set(x: Int, y: Int, piece: Piece) {
        if (!(x in 0..7 && y in 0..7)) {
            return
        }

        this@BoardData.value[x + (y shl 3)] = piece.value
    }

    operator fun set(square: Square, piece: Piece) {
        if (!square.isValid)
            return

        this@BoardData.value[square.value] = piece.value
    }

    fun setUnsafe(x: Int, y: Int, piece: Piece) {
        this@BoardData.value[x + (y shl 3)] = piece.value
    }

    fun setUnsafe(square: Square, piece: Piece) {
        this@BoardData.value[square.value] = piece.value
    }

    fun atUnsafe(x: Int, y: Int): Piece {
        return Piece(value[x + (y shl 3)])
    }

    fun atUnsafe(square: Square): Piece {
        return Piece(value[square.value])
    }

    fun at(x: Int, y: Int): Piece {
        return get(x, y)
    }

    fun at(square: Square): Piece {
        return this[square]
    }
}