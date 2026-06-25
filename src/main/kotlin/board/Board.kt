//@file:Suppress("SpellCheckingInspection")

package board

import main.logic.BoardData
import main.logic.BoardMeta
import main.logic.Move
import main.logic.Piece
import main.logic.PieceType
import main.logic.SpecialMoveType
import main.logic.Square

// A class for representing a board state, as well as the stack of moves that lead to this position
//
// Two stacks are used to efficiently undo moves:
// Move stack: keeps track of made moves
// Meta stack: keeps track of board metadata
//
// A data stack is not used, since the amount of board data is large and would be slow to copy
//


class Board(
    val data: BoardData, var meta: BoardMeta,
    val stack: ArrayDeque<Move>, val metaStack: ArrayDeque<BoardMeta>
) {

    companion object {
        fun fromFen(fen: String): Board {
            val (boardStr, metaStr) = fen.split(' ', limit = 2)
            val (boardData, whiteKing, blackKing) = BoardData.fromFen(boardStr)
            val boardMeta = BoardMeta.fromFen(metaStr, whiteKing, blackKing)
            return Board(boardData, boardMeta, ArrayDeque(), ArrayDeque())
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

        val kingQueenSlidingPatterns = listOf<Pair<Int, Int>>(
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

        val promotionMoveTypes = listOf<SpecialMoveType>(
            SpecialMoveType.PROMOTE_QUEEN,
            SpecialMoveType.PROMOTE_KNIGHT,
            SpecialMoveType.PROMOTE_BISHOP,
            SpecialMoveType.PROMOTE_ROOK
        )
    }

    fun pushMove(move: Move) {
        metaStack.addLast(meta)

        val fromSquare = move.from; val toSquare = move.to

        val pieceToMove = data.atUnsafe(fromSquare)
        var capturedPiece = data.atUnsafe(toSquare)

        data.setUnsafe(toSquare, pieceToMove)
        data.setUnsafe(fromSquare, Piece.EMPTY)
        meta = meta.setEnPassantSquare(Square.NONE)

        when (move.specialMoveType) {
            SpecialMoveType.NONE.value -> { }
            SpecialMoveType.EN_PASSANT.value -> {
                // The en passant square will have the y coordinate of the square we are moving from
                // and the x coordinate of the square we are moving to
                //
                // We need to store the en passant captured piece to be able to undo properly
                // Since the captured piece isn't on the capture square, this will not be done automatically

                val capturedSquare = Square.of(toSquare.x, fromSquare.y)
                capturedPiece = data.atUnsafe(capturedSquare)
                data.setUnsafe(capturedSquare, Piece.EMPTY)
            }

            SpecialMoveType.PAWN_MOVE_TWICE.value -> {
                // Hey, if it works, it works
                meta = meta.setEnPassantSquare(Square.of(fromSquare.x, (fromSquare.y + toSquare.y) shr 1))
            }

            SpecialMoveType.WHITE_CASTLE_KINGSIDE.value -> {
                data.setUnsafe(Square.F1, Piece.WHITE_ROOK)
                data.setUnsafe(Square.H1, Piece.EMPTY)
            }

            SpecialMoveType.WHITE_CASTLE_QUEENSIDE.value -> {
                data.setUnsafe(Square.D1, Piece.WHITE_ROOK)
                data.setUnsafe(Square.A1, Piece.EMPTY)
            }

            SpecialMoveType.BLACK_CASTLE_KINGSIDE.value -> {
                data.setUnsafe(Square.F8, Piece.BLACK_ROOK)
                data.setUnsafe(Square.H8, Piece.EMPTY)
            }

            SpecialMoveType.BLACK_CASTLE_QUEENSIDE.value -> {
                data.setUnsafe(Square.D8, Piece.BLACK_ROOK)
                data.setUnsafe(Square.A8, Piece.EMPTY)
            }

            SpecialMoveType.PROMOTE_KNIGHT.value -> {
                data.setUnsafe(toSquare, Piece.of(PieceType.KNIGHT, pieceToMove.owner))
            }

            SpecialMoveType.PROMOTE_BISHOP.value -> {
                data.setUnsafe(toSquare, Piece.of(PieceType.BISHOP, pieceToMove.owner))
            }

            SpecialMoveType.PROMOTE_ROOK.value -> {
                data.setUnsafe(toSquare, Piece.of(PieceType.ROOK, pieceToMove.owner))
            }

            SpecialMoveType.PROMOTE_QUEEN.value -> {
                data.setUnsafe(toSquare, Piece.of(PieceType.QUEEN, pieceToMove.owner))
            }
        }

        // TODO: turn into special move types for faster lookups
        when (pieceToMove.pieceValue) {
            PieceType.KING.value -> {
                meta = when (pieceToMove.owner) {
                    Player.WHITE -> {
                        meta
                            .setWhiteKing(toSquare)
                            .setWhiteKingsideCastle(false)
                            .setWhiteQueensideCastle(false)
                    }

                    Player.BLACK -> {
                        meta
                            .setBlackKing(toSquare)
                            .setBlackKingsideCastle(false)
                            .setBlackQueensideCastle(false)
                    }

                    Player.NONE -> meta
                }
            }

            PieceType.ROOK.value -> {
                meta = when (fromSquare) {
                    Square.A1 -> meta.setWhiteQueensideCastle(false)
                    Square.H1 -> meta.setWhiteKingsideCastle(false)
                    Square.A8 -> meta.setBlackQueensideCastle(false)
                    Square.H8 -> meta.setBlackKingsideCastle(false)
                    else -> meta
                }
            }
        }

        // DO NOT TOUCH
        when (toSquare) {
            Square.A1 -> meta = meta.setWhiteQueensideCastle(false)
            Square.H1 -> meta = meta.setWhiteKingsideCastle(false)
            Square.A8 -> meta = meta.setBlackQueensideCastle(false)
            Square.H8 -> meta = meta.setBlackKingsideCastle(false)
        }

        // Change turn
        meta = meta.setToMove(meta.notToMove)

        // Undo info is added at push time, to avoid generating unnecessary information during movegen
        stack.addLast(Move.withUndo(move, capturedPiece))
    }

    fun popMove(): Move {
        val move = stack.removeLast()
        val fromSquare = move.from; val toSquare = move.to

        val pieceToMove = data.atUnsafe(toSquare)
        var capturedPiece = move.capturedPiece

        var restorePiece: Boolean = true

        when (move.specialMoveType) {
            SpecialMoveType.NONE.value -> { } // Do nothing

            SpecialMoveType.EN_PASSANT.value -> {
                val capturedSquare = Square.of(toSquare.x, fromSquare.y)
                data.setUnsafe(capturedSquare, capturedPiece)
                capturedPiece = Piece.EMPTY // To prevent the opponents pawn from being restored in th wrong place
            }

            SpecialMoveType.WHITE_CASTLE_KINGSIDE.value -> {
                data.setUnsafe(Square.F1, Piece.EMPTY)
                data.setUnsafe(Square.H1, Piece.WHITE_ROOK)
            }

            SpecialMoveType.WHITE_CASTLE_QUEENSIDE.value -> {
                data.setUnsafe(Square.D1, Piece.EMPTY)
                data.setUnsafe(Square.A1, Piece.WHITE_ROOK)
            }

            SpecialMoveType.BLACK_CASTLE_KINGSIDE.value -> {
                data.setUnsafe(Square.F8, Piece.EMPTY)
                data.setUnsafe(Square.H8, Piece.BLACK_ROOK)
            }

            SpecialMoveType.BLACK_CASTLE_QUEENSIDE.value -> {
                data.setUnsafe(Square.D8, Piece.EMPTY)
                data.setUnsafe(Square.A8, Piece.BLACK_ROOK)
            }

            SpecialMoveType.PROMOTE_KNIGHT.value,
            SpecialMoveType.PROMOTE_BISHOP.value,
            SpecialMoveType.PROMOTE_ROOK.value,
            SpecialMoveType.PROMOTE_QUEEN.value -> {
                data.setUnsafe(fromSquare, Piece.of(PieceType.PAWN, pieceToMove.owner))
                restorePiece = false
            }
        }

        // This needs to be below the `when`, otherwise en passant won't work

        data.setUnsafe(toSquare, capturedPiece)

        if (restorePiece)
            data.setUnsafe(fromSquare, pieceToMove)

        meta = metaStack.removeLast()

        return move
    }

    fun genSlidingMoves(from: Square, addTo: MutableList<Move>, slidingPatterns: List<Pair<Int, Int>>) {
        // TODO: fast sliding by directly offsetting

        for (slidingPattern in slidingPatterns) {
            var currentSquare = from.offset(slidingPattern.first, slidingPattern.second)
            while (currentSquare.isValid) {
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
            if (currentSquare.isValid) {
                if (data.atUnsafe(currentSquare).owner != meta.toMove)
                    addTo.addLast(Move.of(from, currentSquare, SpecialMoveType.NONE))
            }
        }
    }

    fun genKingMoves(from: Square, addTo: MutableList<Move>) {
        val king = data[from]
        for (slidingPattern in kingQueenSlidingPatterns) {
            val currentSquare = from.offset(slidingPattern.first, slidingPattern.second)
            if (currentSquare.isValid) {
                if (data.atUnsafe(currentSquare).owner != meta.toMove)
                    addTo.addLast(Move.of(from, currentSquare, SpecialMoveType.NONE))
            }
        }

        // To prevent the king from castling through check, we check if moving him one square towards the castle would be legal

        when (king.owner) {
            Player.WHITE -> {
                if (
                    meta.whiteKingsideCastle and
                    data[Square.F1].isEmpty and
                    data[Square.G1].isEmpty and
                    !isInCheck(meta.toMove) and
                    isLegal(Move.WHITE_KINGSIDE_CASTLE_PASSTHROUGH)
                    ) {
                    addTo.addLast(Move.WHITE_KINGSIDE_CASTLE)
                }

                if (
                    meta.whiteQueensideCastle and
                    data[Square.D1].isEmpty and
                    data[Square.C1].isEmpty and
                    data[Square.B1].isEmpty and
                    !isInCheck(meta.toMove) and
                    isLegal(Move.WHITE_QUEENSIDE_CASTLE_PASSTHROUGH)
                ) {
                    addTo.addLast(Move.WHITE_QUEENSIDE_CASTLE)
                }
            }
            Player.BLACK -> {
                if (
                    meta.blackKingsideCastle and
                    data[Square.F8].isEmpty and
                    data[Square.G8].isEmpty and
                    !isInCheck(meta.toMove) and
                    isLegal(Move.BLACK_KINGSIDE_CASTLE_PASSTHROUGH)
                ) {
                    addTo.addLast(Move.BLACK_KINGSIDE_CASTLE)
                }

                if (
                    meta.blackQueensideCastle and
                    data[Square.D8].isEmpty and
                    data[Square.C8].isEmpty and
                    data[Square.B8].isEmpty and
                    !isInCheck(meta.toMove) and
                    isLegal(Move.BLACK_QUEENSIDE_CASTLE_PASSTHROUGH)
                ) {
                    addTo.addLast(Move.BLACK_QUEENSIDE_CASTLE)
                }
            }
            else -> {}
        }
    }

    fun genPawnMoves(from: Square, addTo: MutableList<Move>) {
        // TODO: captures

        val pawn = data[from]
        val moveDirection = when (pawn.owner) {
            Player.WHITE -> -1
            Player.BLACK -> 1
            else -> 0 // Illegal
        }

        if ((from.y == 1) or (from.y == 6)) {
            when {
                (((moveDirection == -1 ) and (from.y == 6)) or
                ((moveDirection == 1 ) and (from.y == 1))) -> {
                    val ds = from.yOffset(moveDirection shl 1)
                    // Check
                    if (data[ds].isEmpty and data[from.yOffset(moveDirection)].isEmpty) {
                        addTo.addLast(
                            Move.of(
                            from,
                            from.yOffset(moveDirection shl 1),
                            SpecialMoveType.PAWN_MOVE_TWICE
                        ))
                    }
                }

                (((moveDirection == 1 ) and (from.y == 6)) or
                ((moveDirection == -1 ) and (from.y == 1))) -> {
                    if (data[from.yOffset(moveDirection)].isEmpty) {
                        for (promotionType in promotionMoveTypes) {
                            addTo.addLast(
                                Move.of(
                                from,
                                from.yOffset(moveDirection),
                                promotionType
                            ))
                        }
                    }

                    if (data[from.offset(1, moveDirection)].owner == pawn.owner.opponent()) {
                        // diagonal capture
                        for (promotionType in promotionMoveTypes) {
                            addTo.addLast(
                                Move.of(
                                from,
                                from.offset(1, moveDirection),
                                promotionType
                            ))
                        }
                    }

                    if (data[from.offset(-1, moveDirection)].owner == pawn.owner.opponent()) {
                        // diagonal capture
                        for (promotionType in promotionMoveTypes) {
                            addTo.addLast(
                                Move.of(
                                from,
                                from.offset(-1, moveDirection),
                                promotionType
                            ))
                        }
                    }

                    // Don't add regular non-promotion moves
                    return
                }
            }
        }

        // En passant is added separately

        if (data[from.yOffset(moveDirection)].isEmpty) {
            // one-step-forward move
            addTo.addLast(Move.of(from, from.yOffset(moveDirection), SpecialMoveType.NONE))
        }

        if (data[from.offset(1, moveDirection)].owner == pawn.owner.opponent()) {
            // diagonal capture
            addTo.addLast(Move.of(from, from.offset(1, moveDirection), SpecialMoveType.NONE))
        }

        if (data[from.offset(-1, moveDirection)].owner == pawn.owner.opponent()) {
            // diagonal capture
            addTo.addLast(Move.of(from, from.offset(-1, moveDirection), SpecialMoveType.NONE))
        }
    }

    fun addMovesForSquare(square: Square, addTo: MutableList<Move>) {
        val piece = data.atUnsafe(square)
        if (piece.isEmpty or (piece.owner != meta.toMove)) return

        when (piece.pieceValue) {
            PieceType.PAWN.value -> {
                genPawnMoves(square, addTo)
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
                genSlidingMoves(square, addTo, kingQueenSlidingPatterns)
            }

            PieceType.KING.value -> {
                genKingMoves(square, addTo)
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

        // En passant
        if (meta.enPassantSquare.isValid) {
            val moveDirection = when (meta.toMove) {
                Player.WHITE -> 1
                Player.BLACK -> -1
                else -> 0 // Illegal
            }

            val square = meta.enPassantSquare

            if (data[square.offset(1, moveDirection)]
                == Piece.of(PieceType.PAWN, meta.toMove)) {
                moves.addLast(
                    Move.of(
                    square.offset(1, moveDirection),
                    square,
                    SpecialMoveType.EN_PASSANT
                ))
            }

            if (data[square.offset(-1, moveDirection)]
                == Piece.of(PieceType.PAWN, meta.toMove)) {
                moves.addLast(
                    Move.of(
                    square.offset(-1, moveDirection),
                    square,
                    SpecialMoveType.EN_PASSANT
                ))
            }
        }

        return moves
    }

    fun isInCheck(player: Player): Boolean {
        val king = when (player) {
            Player.WHITE -> meta.whiteKing
            Player.BLACK -> meta.blackKing
            Player.NONE -> Square.NONE
        }

        val checkingPlayer = when (player) {
            Player.WHITE -> Player.BLACK
            Player.BLACK -> Player.WHITE
            Player.NONE -> Player.NONE
        }

        // Bishop + Queen checks

        for (slidingPattern in bishopSlidingPatterns) {
            var currentSquare = king.offset(slidingPattern.first, slidingPattern.second)
            while (currentSquare.isValid) {
                val piece = data.atUnsafe(currentSquare)

                if (piece.isPopulated) {
                    if ((piece.owner == checkingPlayer) and
                        ((piece.pieceValue == PieceType.BISHOP.value) or
                                (piece.pieceValue == PieceType.QUEEN.value))) {
                        // println("Checker: $currentSquare")
                        return true
                    }

                    break
                }

                currentSquare = currentSquare
                    .offset(slidingPattern.first, slidingPattern.second)
            }
        }

        // Rook + Queen checks

        for (slidingPattern in rookSlidingPatterns) {
            var currentSquare = king.offset(slidingPattern.first, slidingPattern.second)
            while (currentSquare.isValid) {
                val piece = data.atUnsafe(currentSquare)

                if (piece.isPopulated) {
                    if ((piece.owner == checkingPlayer) and
                        ((piece.pieceValue == PieceType.ROOK.value) or
                                (piece.pieceValue == PieceType.QUEEN.value))) {
                        // println("Checker: $currentSquare")
                        return true
                    }

                    break
                }

                currentSquare = currentSquare
                    .offset(slidingPattern.first, slidingPattern.second)
            }
        }

        // Knight checks

        for (slidingPattern in knightSlidingPatterns) {
            val currentSquare = king.offset(slidingPattern.first, slidingPattern.second)
            if (currentSquare.isValid) {
                val piece = data[currentSquare]
                if (
                    piece.isPopulated and
                    (piece.owner == checkingPlayer) and
                    (piece.pieceValue == PieceType.KNIGHT.value)
                    ) {
                    // println("Checker: $currentSquare")
                    return true
                }
            }
        }

        // King "checks"

        for (slidingPattern in kingQueenSlidingPatterns) {
            val currentSquare = king.offset(slidingPattern.first, slidingPattern.second)
            if (currentSquare.isValid) {
                val piece = data[currentSquare]
                // Never mind
                if (
                    piece.isPopulated and
                    (piece.owner == checkingPlayer) and
                    (piece.pieceValue == PieceType.KING.value)
                ) {
                    // println("Checker: $currentSquare")
                    return true
                }
            }
        }

        // Pawn checks

        when (player) {
            Player.WHITE -> {
                // To allow `val` usage
                run {
                    val pos = king.offset(1, -1)
                    if (pos.isValid and (data[pos] == Piece.BLACK_PAWN)) {
                        // println("Checker: $pos")
                        return true
                    }
                }

                run {
                    val pos = king.offset(-1, -1)
                    if (pos.isValid and (data[pos] == Piece.BLACK_PAWN)) {
                        // println("Checker: $pos")
                        return true
                    }
                }
            }
            Player.BLACK -> {
                run {
                    val pos = king.offset(1, 1)
                    if (pos.isValid and (data[pos] == Piece.WHITE_PAWN)) {
                        // println("Checker: $pos")
                        return true
                    }
                }

                run {
                    val pos = king.offset(-1, 1)
                    if (pos.isValid and (data[pos] == Piece.WHITE_PAWN)) {
                        // println("Checker: $pos")
                        return true
                    }
                }
            }
            else -> {return false}
        }

        return false
    }

    fun isLegal(move: Move): Boolean {

        pushMove(move)
        val res = !isInCheck(meta.notToMove)
        popMove()

        return res
    }

    override fun toString(): String {
        return meta.toString() + "\n" + data.toString()
    }

    fun generateLegalMoves(): List<Move> {
        val pseudoLegalMoves = generatePseudoLegalMoves()
        val legalMoves: MutableList<Move> = mutableListOf()
        for (pseudoLegalMove in pseudoLegalMoves) {
            if (isLegal(pseudoLegalMove)) {
                legalMoves.addLast(pseudoLegalMove)
            }
        }
        return legalMoves
    }

    fun perftBulkCount(depth: Int): Int {
        // PERFormance Test, move path enumeration
        // with bulk counting
        val legalMoves = generateLegalMoves()

        if (depth == 1)
            return legalMoves.size

        var sum = 0
        for (move in legalMoves) {
            pushMove(move)
            sum += perftBulkCount(depth - 1)
            popMove()
        }
        return sum
    }

    fun perft(depth: Int): Int {
        // PERFormance Test, move path enumeration
        val legalMoves = generateLegalMoves()

        if (depth == 0)
            return 1

        var sum = 0
        for (move in legalMoves) {
            pushMove(move)
            sum += perft(depth - 1)
            popMove()
        }
        return sum
    }

    fun perftVerbose(depth: Int) {
        println(this)

        var tot = 0

        for (move in generateLegalMoves()) {
            pushMove(move)
            val res = perft(depth - 1)
            tot += res
            popMove()
            println("$move: $res")
            println(this)
        }

        println("Total nodes visited: $tot")
    }
}
