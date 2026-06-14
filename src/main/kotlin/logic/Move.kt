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


@JvmInline
value class BoardMeta(val value: ULong) {

    // B0-1: Full move
    // B2: Half move (caps at 50)
    // B3: to move (0000 0010: white, 0000 0001 black),
    // B4: en passant square
    // B5: Castling (WK, WQ, BK, BQ)
    // B6: White king position
    // B7: Black king position

    val fullMove:
            Int get() = (value and 0x00_00_00_00_00_00_FF_FFuL).toInt()

    val halfMove:
            Byte get() = ((value and 0x00_00_00_00_00_FF_00_00uL) shr 16).coerceIn(0uL, 50uL).toByte()

    val toMove:
            Player get() = when {
        (value and 0x00_00_00_00_02_00_00_00u) != 0uL -> Player.WHITE
        value and 0x00_00_00_00_01_00_00_00u != 0uL -> Player.BLACK
        else -> Player.NONE
    }

    val enPassantSquare:
            Square get() = Square(((value and 0x00_00_00_FF_00_00_00_00uL) shr 32).toInt())

    val whiteKingsideCastle:
            Boolean get() = (value and 0x00_00_01_00_00_00_00_00uL) != 0uL
    val whiteQueensideCastle:
            Boolean get() = (value and 0x00_00_02_00_00_00_00_00uL) != 0uL
    val blackKingsideCastle:
            Boolean get() = (value and 0x00_00_04_00_00_00_00_00uL) != 0uL
    val blackQueensideCastle:
            Boolean get() = (value and 0x00_00_08_00_00_00_00_00uL) != 0uL

    val whiteKing:
            Square get() = Square(((value and 0x00_FF_00_00_00_00_00_00uL) shr 48).toInt())

    val blackKing:
            Square get() = Square(((value and 0xFF_00_00_00_00_00_00_00uL) shr 56).toInt())

    companion object {
        private fun Boolean.toULong(mask: ULong) = if (this) mask else 0uL

        fun of(
            fullMove: Short,
            halfMove: Byte,
            toMove: Player,
            enPassantSquare: Square,
            whiteKingsideCastle: Boolean,
            whiteQueensideCastle: Boolean,
            blackKingsideCastle: Boolean,
            blackQueensideCastle: Boolean,
            whiteKing: Square,
            blackKing: Square
        ): BoardMeta {
            val toMoveBits = when (toMove) {
                Player.WHITE -> 0x01uL
                Player.BLACK -> 0x02uL
                else -> 0x00uL
            }
            return BoardMeta(
                fullMove.toULong()                                          // B0–1 : full move
                        or (halfMove.toULong() shl 16)                             // B1   : half move  (if re-laid out)
                        or (toMoveBits shl 24)                                    // B3   : to move
                        or (enPassantSquare.value.toULong() shl 32)                 // B4   : en passant
                        or whiteKingsideCastle.toULong(0x01uL shl 40)      // B2   : WK castle
                        or whiteQueensideCastle.toULong(0x02uL shl 40)     // B2   : WQ castle
                        or blackKingsideCastle.toULong(0x04uL shl 40)      // B2   : BK castle
                        or blackQueensideCastle.toULong(0x08uL shl 40)     // B2   : BQ castle
                        or (whiteKing.value.toULong() shl 48)                       // B6   : white king
                        or (blackKing.value.toULong() shl 56)                       // B7   : black king
            )
        }

        fun fromFen(fen: String, whiteKing: Square, blackKing: Square): BoardMeta {
            val (toPlay, castlingAvailability, enPassantSquare, halfMoveClock, fullMoveClock) =
                fen.split(' ', limit = 5)

            val toPlayPlayer = when (toPlay.lowercase()) {
                "w" -> Player.WHITE
                "b" -> Player.BLACK
                else -> Player.NONE
            }

            var whiteKingsideCastle = false
            var whiteQueensideCastle = false
            var blackKingsideCastle = false
            var blackQueensideCastle = false

            for (ch in castlingAvailability.toCharArray()) {
                when (ch) {
                    'K' -> whiteKingsideCastle = true
                    'Q' -> whiteQueensideCastle = true
                    'k' -> blackKingsideCastle = true
                    'q' -> blackQueensideCastle = true
                }
            }

            return BoardMeta.of(
                fullMove = fullMoveClock.toShort(),
                halfMove = halfMoveClock.toByte(),
                toMove = toPlayPlayer,
                enPassantSquare = Square.fromString(enPassantSquare),
                whiteKingsideCastle = whiteKingsideCastle,
                whiteQueensideCastle = whiteQueensideCastle,
                blackKingsideCastle = blackKingsideCastle,
                blackQueensideCastle = blackQueensideCastle,
                whiteKing = whiteKing,
                blackKing = blackKing
            )
        }
    }
}