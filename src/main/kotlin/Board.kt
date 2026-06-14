@file:Suppress("SpellCheckingInspection")

package main

enum class Player(val value: Int) {
    WHITE(1),
    BLACK(-1),
    NONE(0)
}

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
            String = "main.Piece(${owner} ${PieceType.fromValue(pieceValue)})"

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

@JvmInline
value class Square(val value: Int) {
    // x011  x110
    // from  to

    val x:
            Int get() = value and 0x07
    val y:
            Int get() = (value and 0x70) shr 4

    fun offset(xOffset: Int, yOffset: Int): Square {
        return Square((value + xOffset + (yOffset shl 4)) and 0x77)
    }

    companion object {
        fun of(x: Int, y: Int): Square = Square((y shl 4) or x)

        fun fromString(string: String): Square = Square.of(
            x = "ABCDEFGI".indexOf(string[0].lowercase()),
            y = "87654321".indexOf(string[0])
        )
    }
}

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
        val str = "main.Board\n  A B C D E F G H I\n\n"

        return  str
    }
}