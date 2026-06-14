@file:Suppress("SpellCheckingInspection")

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

    operator fun get(x: Int, y: Int): Piece {
        if (!(x in 0..7 && y in 0..7)) {
            return Piece.EMPTY
        }

        return Piece(value[x + (y * 8)])
    }

    operator fun get(square: Square): Piece {
        return get(square.x, square.y)
    }

    operator fun set(x: Int, y: Int, piece: Piece) {
        if (!(x in 0..7 && y in 0..7)) {
            return
        }

        this@BoardData.value[x + (y * 8)] = piece.value
    }

    operator fun set(square: Square, piece: Piece) {
        return set(square.x, square.y, piece)
    }

    fun setUnsafe(x: Int, y: Int, piece: Piece) {
        this@BoardData.value[x + (y * 8)] = piece.value
    }

    fun setUnsafe(square: Square, piece: Piece) {
        this@BoardData.value[square.x + (square.y * 8)] = piece.value
    }

    fun atUnsafe(x: Int, y: Int): Piece {
        return Piece(value[x + (y * 8)])
    }

    fun atUnsafe(square: Square): Piece {
        return Piece(value[square.x + (square.y * 8)])
    }

    fun at(x: Int, y: Int): Piece {
        return get(x, y)
    }

    fun at(square: Square): Piece {
        return get(square.x, square.y)
    }
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
            Int
        get() = (value and 0x00_00_00_00_00_00_FF_FFuL).toInt()

    fun setFullMove(newValue: Int): BoardMeta =
        BoardMeta((value and 0xFF_FF_FF_FF_FF_FF_00_00uL) or newValue.toULong())


    val halfMove:
            Byte
        get() = ((value and 0x00_00_00_00_00_FF_00_00uL) shr 16).coerceIn(0uL, 50uL).toByte()

    fun setHalfMove(newValue: Int): BoardMeta =
        BoardMeta((value and 0xFF_FF_FF_FF_FF_00_FF_FFuL) or (newValue.toULong() shl 16))


    val toMove:
            Player
        get() = when {
            (value and 0x00_00_00_00_01_00_00_00uL) != 0uL -> Player.WHITE
            value and 0x00_00_00_00_02_00_00_00uL != 0uL -> Player.BLACK
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

    fun setEnPassantSquare(newValue: Int): BoardMeta =
        BoardMeta((value and 0xFF_FF_FF_00_FF_FF_FF_FFuL) or (newValue.toULong() shl 32))


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

    val whiteKing:
            Square
        get() = Square(((value and 0x00_FF_00_00_00_00_00_00uL) shr 48).toInt())
    fun setWhiteKing(newValue: Int): BoardMeta =
        BoardMeta((value and 0xFF_00_FF_FF_FF_FF_FF_FFuL) or (newValue.toULong() shl 48))

    val blackKing:
            Square
        get() = Square(((value and 0xFF_00_00_00_00_00_00_00uL) shr 56).toInt())
    fun setBlackKing(newValue: Int): BoardMeta =
        BoardMeta((value and 0x00_FF_FF_FF_FF_FF_FF_FFuL) or (newValue.toULong() shl 56))

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


class Board(val data: BoardData, val meta: BoardMeta, val stack: ArrayDeque<Move>) {
    companion object {
        fun fromFen(fen: String): Board {
            val (boardStr, metaStr) = fen.split(' ', limit = 2)
            val (boardData, whiteKing, blackKing) = BoardData.fromFen(boardStr)
            val boardMeta = BoardMeta.fromFen(metaStr, whiteKing, blackKing)
            return Board(boardData, boardMeta, ArrayDeque())
        }

        fun startingPosition(): Board {
            return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        }
    }

    fun pushMove(move: Move) {


        val fromSquare = move.from; val toSquare = move.to

        val pieceToMove = data.atUnsafe(fromSquare)
        val capturedPiece = data.atUnsafe(toSquare)

        data.setUnsafe(toSquare, pieceToMove)
        data.setUnsafe(fromSquare, Piece.EMPTY)

        // Undo info is added at push time, to avoid generating unnecessary information during movegen
        stack.addLast(Move.withUndo(move, capturedPiece))

        when (move.specialMoveType) {
            SpecialMoveType.NONE.value -> return
            SpecialMoveType.EN_PASSANT.value -> {
                // One will be the square the opponents pawn moved from, one the one it moved to
                // Therefore, no extra verification is required
                // Using `yOffset` is slower, since we to unpack anyway
                data.setUnsafe(fromSquare.x, fromSquare.y + 1, Piece.EMPTY)
                data.setUnsafe(fromSquare.x, fromSquare.y - 1, Piece.EMPTY)
            }
            // TODO: impl
        }
    }

    fun popMove(): Move {
        val popedMove = stack.removeLast()
        val fromSquare = popedMove.from; val toSquare = popedMove.to

        val pieceToMove = data.atUnsafe(toSquare)
        val capturedPiece = popedMove.capturedPiece
        data.setUnsafe(toSquare, capturedPiece)
        data.setUnsafe(fromSquare, pieceToMove)

        when (popedMove.specialMoveType) {
            SpecialMoveType.NONE.value -> Unit // Do nothing
            // TODO: impl
        }

        return popedMove
    }

    fun addMovesForSquare(x: Int, y: Int, addTo: MutableList<Move>) {
        val piece = data.atUnsafe(x, y)
        if (piece.isEmpty or (piece.owner != meta.toMove)) return

        when (piece.pieceValue) {
            PieceType.PAWN.value -> {
                if (meta.enPassantSquare)
            }
        }
    }

    fun generatePseudoLegalMoves(): MutableList<Move> {
        val moves = mutableListOf<Move>()
        for (x in 0..7) {
            for (y in 0..7) {
                addMovesForSquare(x, y, moves)
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