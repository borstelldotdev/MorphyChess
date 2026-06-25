package main.logic

import board.Player

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

    val owner: Player
        get() = when {
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
                'P' -> WHITE_PAWN
                'N' -> WHITE_KNIGHT
                'B' -> WHITE_BISHOP
                'R' -> WHITE_ROOK
                'Q' -> WHITE_QUEEN
                'K' -> WHITE_KING
                'p' -> BLACK_PAWN
                'n' -> BLACK_KNIGHT
                'b' -> BLACK_BISHOP
                'r' -> BLACK_ROOK
                'q' -> BLACK_QUEEN
                'k' -> BLACK_KING

                else -> EMPTY
            }
        }

        // Empty square
        val EMPTY = Piece(0)

        val WHITE_PAWN = of(PieceType.PAWN, Player.WHITE)
        val WHITE_KNIGHT = of(PieceType.KNIGHT, Player.WHITE)
        val WHITE_BISHOP = of(PieceType.BISHOP, Player.WHITE)
        val WHITE_ROOK = of(PieceType.ROOK, Player.WHITE)
        val WHITE_QUEEN = of(PieceType.QUEEN, Player.WHITE)
        val WHITE_KING = of(PieceType.KING, Player.WHITE)
        val BLACK_PAWN = of(PieceType.PAWN, Player.BLACK)
        val BLACK_KNIGHT = of(PieceType.KNIGHT, Player.BLACK)
        val BLACK_BISHOP = of(PieceType.BISHOP, Player.BLACK)
        val BLACK_ROOK = of(PieceType.ROOK, Player.BLACK)
        val BLACK_QUEEN = of(PieceType.QUEEN, Player.BLACK)
        val BLACK_KING = of(PieceType.KING, Player.BLACK)
    }
}