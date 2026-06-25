package main.logic

import board.Player


@JvmInline
value class BoardMeta(val value: ULong) {

    // B0-1: Full move
    // B2: Half move (caps at 150 (75 for each))
    // B3: to move (0000 0010: white, 0000 0001 black),
    // B4: en passant square
    // B5: Castling (WK, WQ, BK, BQ)
    // B6: White king position
    // B7: Black king position

    val fullMove:
            Int
        get() = (value and 0x00_00_00_00_00_00_FF_FFuL).toInt()

    fun setFullMove(newValue: Int): BoardMeta =
        BoardMeta((value and 0xFF_FF_FF_FF_FF_FF_00_00uL) or newValue.toULong())

    fun incrementFullMove(): BoardMeta =
        BoardMeta(value + 1uL)

    fun decrementFullMove(): BoardMeta =
        BoardMeta(value - 1uL)

    val halfMove:
            Byte
        get() = ((value and 0x00_00_00_00_00_FF_00_00uL) shr 16).coerceIn(0uL, 50uL).toByte()

    fun setHalfMove(newValue: Int): BoardMeta =
        BoardMeta((value and 0xFF_FF_FF_FF_FF_00_FF_FFuL) or (newValue.toULong() shl 16))

    fun incrementHalfMove(): BoardMeta =
        BoardMeta(value + 0x1_00_00uL)

    fun decrementHalfMove(): BoardMeta =
        BoardMeta(value - 0x1_00_00uL)

    fun resetHalfMove(): BoardMeta =
        BoardMeta(value and 0xFF_FF_FF_FF_FF_00_FF_FFuL)

    // TODO: Optimize to move storage, by packing directly

    val toMove: Player
        get() = when {
            (value and 0x00_00_00_00_01_00_00_00uL) != 0uL -> Player.WHITE
            value and 0x00_00_00_00_02_00_00_00uL != 0uL -> Player.BLACK
            else -> Player.NONE
        }

    val notToMove: Player
        get() = when {
            (value and 0x00_00_00_00_01_00_00_00uL) != 0uL -> Player.BLACK
            value and 0x00_00_00_00_02_00_00_00uL != 0uL -> Player.WHITE
            else -> Player.NONE
        }

    fun setToMove(newValue: Player): BoardMeta =
        BoardMeta(
            (value and 0xFF_FF_FF_FF_00_FF_FF_FFuL) or when (newValue) {
                Player.WHITE -> 0x00_00_00_00_01_00_00_00uL
                Player.BLACK -> 0x00_00_00_00_02_00_00_00uL
                else -> 0uL
            }
        )


    val enPassantSquare:
            Square
        get() = Square(((value and 0x00_00_00_FF_00_00_00_00uL) shr 32).toInt())

    fun setEnPassantSquare(newValue: Square): BoardMeta =
        BoardMeta((value and 0xFF_FF_FF_00_FF_FF_FF_FFuL) or (newValue.value.toULong() shl 32))


    val whiteKingsideCastle:
            Boolean
        get() = (value and 0x00_00_01_00_00_00_00_00uL) != 0uL

    fun setWhiteKingsideCastle(newValue: Boolean): BoardMeta =
        BoardMeta((value and 0xFF_FF_0E_FF_FF_FF_FF_FFuL) or (newValue.toULong(0x00_00_01_00_00_00_00_00uL)))

    val whiteQueensideCastle:
            Boolean
        get() = (value and 0x00_00_02_00_00_00_00_00uL) != 0uL

    fun setWhiteQueensideCastle(newValue: Boolean): BoardMeta =
        BoardMeta((value and 0xFF_FF_0D_FF_FF_FF_FF_FFuL) or (newValue.toULong(0x00_00_02_00_00_00_00_00uL)))

    val blackKingsideCastle:
            Boolean
        get() = (value and 0x00_00_04_00_00_00_00_00uL) != 0uL

    fun setBlackKingsideCastle(newValue: Boolean): BoardMeta =
        BoardMeta((value and 0xFF_FF_0B_FF_FF_FF_FF_FFuL) or (newValue.toULong(0x00_00_04_00_00_00_00_00uL)))

    val blackQueensideCastle:
            Boolean
        get() = (value and 0x00_00_08_00_00_00_00_00uL) != 0uL

    fun setBlackQueensideCastle(newValue: Boolean): BoardMeta =
        BoardMeta((value and 0xFF_FF_07_FF_FF_FF_FF_FFuL) or (newValue.toULong(0x00_00_08_00_00_00_00_00uL)))

    fun generateCastlingRightsString(): String {
        var str = ""

        if (whiteKingsideCastle) str += "K"
        if (whiteQueensideCastle) str += "Q"
        if (blackKingsideCastle) str += "k"
        if (blackQueensideCastle) str += "q"

        if (str.isEmpty()) str = "-"

        return str
    }

    override fun toString(): String {
        return """
        To move: $toMove
        Full move clock: $fullMove
        Half move clock: $halfMove
        En passant square: $enPassantSquare
        Castling rights: ${generateCastlingRightsString()}
        White king position: $whiteKing
        Black king position: $blackKing
        """
    }

    val whiteKing:
            Square
        get() = Square(((value and 0x00_FF_00_00_00_00_00_00uL) shr 48).toInt())
    fun setWhiteKing(newValue: Square): BoardMeta =
        BoardMeta((value and 0xFF_00_FF_FF_FF_FF_FF_FFuL) or (newValue.value.toULong() shl 48))

    val blackKing:
            Square
        get() = Square(((value and 0xFF_00_00_00_00_00_00_00uL) shr 56).toInt())
    fun setBlackKing(newValue: Square): BoardMeta =
        BoardMeta((value and 0x00_FF_FF_FF_FF_FF_FF_FFuL) or (newValue.value.toULong() shl 56))

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
                fullMove.toULong()                                           // B0–1 : full move
                        or (halfMove.toULong() shl 16)                              // B2   : half move
                        or (toMoveBits shl 24)                                      // B3  : to move
                        or (enPassantSquare.value.toULong() shl 32)                 // B4   : en passant
                        or whiteKingsideCastle.toULong(0x01uL shl 40)       // B5   : WK castle
                        or whiteQueensideCastle.toULong(0x02uL shl 40)      // B5   : WQ castle
                        or blackKingsideCastle.toULong(0x04uL shl 40)       // B5   : BK castle
                        or blackQueensideCastle.toULong(0x08uL shl 40)      // B5   : BQ castle
                        or (whiteKing.value.toULong() shl 48)                      // B6   : white king
                        or (blackKing.value.toULong() shl 56)                      // B7   : black king
            )
        }

        fun fromFen(fen: String, whiteKing: Square, blackKing: Square): BoardMeta {
            val (toPlay, castlingAvailability, enPassantSquare, halfMoveClock, fullMoveClock) =
                fen.trim().split(' ', limit = 5)

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
