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

    val owner: Player get() = when {
        value > 0 -> Player.WHITE
        value < 0 -> Player.BLACK
        else    -> Player.NONE
    }

    val pieceValue:
            Int get() = value * owner.value

    val isEmpty:
            Boolean get() = value == 0

    override fun toString():
            String = "main.logic.Piece(${owner} ${PieceType.fromValue(pieceValue)})"

    fun toUB(): Int = when {
        value > 0 -> value
        value < 0 -> 0x10 - value
        else -> 0
    }

    companion object {
        fun of(type: PieceType, owner: Player): Piece {
            return Piece(type.value * owner.value)
        }

        fun fromUB(ub: Int): Piece = Piece(
            when {
                (ub and 0x10) != 0 -> ub and 0x0F
                else -> ub
            })

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