//@file:Suppress("SpellCheckingInspection")

package main.logic


class Board(val data: BoardData, var meta: BoardMeta, val stack: ArrayDeque<Move>) {
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

        val rookSlidingPatterns = listOf<Pair<Int, Int>>(
            Pair(1, 0),
            Pair(-1, 0),
            Pair(0, 1),
            Pair(0, -1),
        )

        val bishopSlidingPatterns = listOf<Pair<Int, Int>>(
            Pair(1, 1),
            Pair(-1, 1),
            Pair(1, -1),
            Pair(-1, -1),
        )

        val queenSlidingPatterns = listOf<Pair<Int, Int>>(
            Pair(1, 0),
            Pair(-1, 0),
            Pair(0, 1),
            Pair(0, -1),

            Pair(1, 1),
            Pair(-1, 1),
            Pair(1, -1),
            Pair(-1, -1),
        )

        val knightSlidingPatterns = listOf<Pair<Int, Int>>(
            Pair(1, 2),
            Pair(2, 1),
            Pair(-1, 2),
            Pair(-2, 1),
            Pair(1, -2),
            Pair(2, -1),
            Pair(-1, -2),
            Pair(-2, -1),
        )
    }

    fun pushMove(move: Move) {
        val fromSquare = move.from; val toSquare = move.to

        val pieceToMove = data.atUnsafe(fromSquare)
        var capturedPiece = data.atUnsafe(toSquare)

        data.setUnsafe(toSquare, pieceToMove)
        data.setUnsafe(fromSquare, Piece.EMPTY)

        when (move.specialMoveType) {
            SpecialMoveType.NONE.value -> return
            SpecialMoveType.EN_PASSANT.value -> {
                // One will be the square the opponents pawn moved from, one the one it moved to
                // Therefore, no extra verification is required to ensure we aren't removing other pieces
                // Using `yOffset` is slower, since we to unpack anyway
                //
                // We need to store the en passant captured piece to be able to undo properly
                // Since the captured piece insn't on the capture square, this will not be done automaticly
                if (data.atUnsafe(fromSquare.x, fromSquare.y + 1).isPopulated) {
                    // Overhead from re-querrying is minimal beacuase en passant is rare
                    capturedPiece = data.atUnsafe(fromSquare.x, fromSquare.y + 1)
                    data.setUnsafe(fromSquare.x, fromSquare.y + 1, Piece.EMPTY)
                } else {
                    capturedPiece = data.atUnsafe(fromSquare.x, fromSquare.y - 1)
                    data.setUnsafe(fromSquare.x, fromSquare.y - 1, Piece.EMPTY)
                }
            }

            SpecialMoveType.PAWN_MOVE_TWICE.value -> {
                // Hey, if it works, it works
                meta = meta.setEnPassantSquare(Square.of(fromSquare.x, (fromSquare.y + toSquare.y) shr 1))
            }

            // TODO: impl more
        }

        // Undo info is added at push time, to avoid generating unnecessary information during movegen
        stack.addLast(Move.withUndo(move, capturedPiece))
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

    fun genSlidingMoves(from: Square, addTo: MutableList<Move>, slidingPatterns: List<Pair<Int, Int>>) {
        for (slidingPattern in slidingPatterns) {
            var currentSquare = from.offset(slidingPattern.first, slidingPattern.second)
            while (currentSquare.exists) {
                val piece = data.atUnsafe(currentSquare)

                if (piece.owner == meta.toMove)
                    break

                addTo.addLast(Move.of(from, currentSquare, SpecialMoveType.NONE))

                if (piece.owner == meta.notToMove)
                    break

                currentSquare = currentSquare
                    .offset(slidingPattern.first, slidingPattern.second)
            }
        }
    }

    fun genKnightMoves(from: Square, addTo: MutableList<Move>) {
        for (slidingPattern in knightSlidingPatterns) {
            val currentSquare = from.offset(slidingPattern.first, slidingPattern.second)
            if (currentSquare.exists) {
                if (data.atUnsafe(currentSquare).owner != meta.toMove)
                    addTo.addLast(Move.of(from, currentSquare, SpecialMoveType.NONE))
            }
        }
    }

    fun addMovesForSquare(square: Square, addTo: MutableList<Move>) {
        val piece = data.atUnsafe(square)
        if (piece.isEmpty or (piece.owner != meta.toMove)) return

        when (piece.pieceValue) {
            PieceType.PAWN.value -> {
                Unit // todo: impl
            }

            PieceType.KNIGHT.value -> {
                genKnightMoves(square, addTo)
            }

            PieceType.BISHOP.value -> {
                genSlidingMoves(square, addTo, bishopSlidingPatterns)
            }

            PieceType.ROOK.value -> {
                genSlidingMoves(square, addTo, rookSlidingPatterns)
            }

            PieceType.QUEEN.value -> {
                genSlidingMoves(square, addTo, queenSlidingPatterns)
            }
        }
    }

    fun generatePseudoLegalMoves(): MutableList<Move> {
        val moves = mutableListOf<Move>()
        for (x in 0..7) {
            for (y in 0..7) {
                addMovesForSquare(Square.ofUnsafe(x, y), moves)
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
            pushMove(move)
            sum += perft(depth - 1)
            popMove()
        }
        return sum
    }


    override fun toString(): String {
        // TODO: fix
        val str = "main.logic.Board\n  A B C D E F G H I\n\n"

        return  str
    }
}