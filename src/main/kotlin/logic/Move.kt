package main.logic

enum class SpecialMoveType(val value: Int) {
    NONE(0),

    WHITE_CASTLE_KINGSIDE(10),
    WHITE_CASTLE_QUEENSIDE(11),
    BLACK_CASTLE_KINGSIDE(12),
    BLACK_CASTLE_QUEENSIDE(13),

    PROMOTE_KNIGHT(20),
    PROMOTE_BISHOP(21),
    PROMOTE_ROOK(22),
    PROMOTE_QUEEN(23),

    EN_PASSANT(30),
    PAWN_MOVE_TWICE(40),
}

@JvmInline
value class Move(val value: Int) {
    // MSB <- ... -> LSB
    // 0x   00                          00          00  00
    //      captured piece (undo info)  special     to  from
    // 0 = null move

    companion object {
        fun of(from: Square, to: Square, specialMoveType: SpecialMoveType): Move =
            Move(from.value or (to.value shl 8) or (specialMoveType.value shl 16))

        /*fun fromString(string: String): Square {
            assert(string.length >= 4)
            val to = Square.fromString(string.slice())
        }*/

        fun withUndo(original: Move, capturedPiece: Piece) =
            Move(original.value or (capturedPiece.value shl 24))

        val NONE = Move(0)

        val WHITE_KINGSIDE_CASTLE = of(Square.E1, Square.G1, SpecialMoveType.WHITE_CASTLE_KINGSIDE)
        val WHITE_QUEENSIDE_CASTLE = of(Square.E1, Square.C1, SpecialMoveType.WHITE_CASTLE_QUEENSIDE)
        val BLACK_KINGSIDE_CASTLE = of(Square.E8, Square.G8, SpecialMoveType.BLACK_CASTLE_KINGSIDE)
        val BLACK_QUEENSIDE_CASTLE = of(Square.E8, Square.C8, SpecialMoveType.BLACK_CASTLE_QUEENSIDE)

    }

    val from:
            Square get() = Square(value and 0xFF)

    val to:
            Square get() = Square((value shr 8) and 0xFF)

    val specialMoveType:
            Int get() = (value shr 16) and 0xFF

    val capturedPiece:
            Piece get() = Piece(value shr 24) // `and` redundant since `int` is 32 bits

    val isNullMove:
            Boolean get() = value == 0
    val isValid:
            Boolean get() = value != 0

    override fun toString(): String {
        if (isNullMove)
            return "Move(None)"

        return "Move(from: $from, to: $to, captured piece: $capturedPiece" +
                (if (specialMoveType != SpecialMoveType.NONE.value) "special move type: $specialMoveType)" else "")
    }
}
