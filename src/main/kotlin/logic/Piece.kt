package main.logic

enum class PieceType(val value: Int) {
    PAWN(1),
    KNIGHT(2),
    BISHOP(3),
    ROOK(4),
    QUEEN(5),
    KING(6);


    companion object {
        fun fromValue(v: Int) = entries.firstOrNull { it.value == v} ?: error("Unknown piece value: $v")
    }
}

@JvmInline
value class Piece(val value: Int) {
    // MSB <- ... -> LSB
    // ... owner-white(1b) owner-black (1b) type(4b)

    val owner: Player get() = when {
        value == 0              -> Player.NONE
        (value and 0x20) != 0   -> Player.WHITE
        (value and 0x10) != 0   -> Player.BLACK
        else                    -> Player.NONE

    }

    val pieceValue:
            Int get() = value and 0xF

    val isEmpty:
            Boolean get() = value == 0
    val isPopulated:
            Boolean get() = value != 0

    override fun toString(): String {
        if (isEmpty) {
            return "Piece(None)"
        }

        return "Piece($owner ${PieceType.fromValue(pieceValue)})"
    }

    companion object {
        fun of(type: PieceType, owner: Player): Piece {
            return Piece(type.value or owner.value)
        }

        fun fromChar(ch: Char): Piece {
            return when (ch) {
                'P' -> of(PieceType.PAWN, Player.WHITE)
                'N' -> of(PieceType.KNIGHT, Player.WHITE)
                'B' -> of(PieceType.BISHOP, Player.WHITE)
                'R' -> of(PieceType.ROOK, Player.WHITE)
                'Q' -> of(PieceType.QUEEN, Player.WHITE)
                'K' -> of(PieceType.KING, Player.WHITE)

                'p' -> of(PieceType.PAWN, Player.BLACK)
                'n' -> of(PieceType.KNIGHT, Player.BLACK)
                'b' -> of(PieceType.BISHOP, Player.BLACK)
                'r' -> of(PieceType.ROOK, Player.BLACK)
                'q' -> of(PieceType.QUEEN, Player.BLACK)
                'k' -> of(PieceType.KING, Player.BLACK)

                else -> EMPTY
            }
        }

        // Empty square
        val EMPTY = Piece(0)
    }
}