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
    // 0x   00      00      00          00
    //      from    to      special     captured piece (undo info)

    companion object {
        fun of(from: Square, to: Square, specialMoveType: SpecialMoveType): Move =
            Move(from.value or (to.value shl 8) or (specialMoveType.value shl 16))

        /*fun fromString(string: String): Square {
            assert(string.length >= 4)
            val to = Square.fromString(string.slice())
        }*/

        fun withUndo(original: Move, capturedPiece: Piece) =
            Move(original.value or capturedPiece.toUB())
    }

    val from:
            Square get() = Square(value and 0xFF)

    val to:
            Square get() = Square((value shr 8) and 0xFF)

    val specialMoveType:
            Int get() = (value shr 16) and 0xFF

    val capturedPiece:
            Piece get() = Piece.fromUB(value shr 24) // `and` redundant since `int` is 32 bits
}
