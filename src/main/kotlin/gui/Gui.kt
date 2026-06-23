package main.gui

import main.logic.Board
import main.logic.Move
import main.logic.Piece
import main.logic.PieceType
import main.logic.Player
import main.logic.SpecialMoveType
import main.logic.Square
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ChessBoard(val board: Board) : JPanel(), MouseListener, MouseMotionListener, KeyListener {
    companion object {
        fun loadImage(path: String): BufferedImage {
            val stream = object {}.javaClass.getResourceAsStream(path)
                ?: error("Resource not found: $path")
            return ImageIO.read(stream)
        }

        val pieceSpriteMap: Map<Piece, BufferedImage> = buildMap {
            for (player in Player.entries) {
                if (player == Player.NONE) continue

                for (type in PieceType.entries) {
                    val color = player.name.lowercase()
                    val piece = type.name.lowercase()
                    val img = loadImage("/image_assets/$piece-$color.png")
                    put(Piece.of(type, player), img)
                }
            }
        }

        var selectedSquare: Square? = null
        val highlightedSquares: MutableList<Square> = mutableListOf()

        val BACKGROUND_COLOR =                  Color(48, 48, 48)
        val LIGHT_SQUARE_COLOR =                Color(235, 210, 175)
        val DARK_SQUARE_COLOR =                 Color(185, 135, 100)
        val SELECTED_LIGHT_SQUARE_COLOR =       Color(245, 235, 115)
        val SELECTED_DARK_SQUARE_COLOR =        Color(220, 190, 75)
        val SELECTED_SQUARE_OUTLINE =           Color(80, 80, 80)
        val LIGHT_HIGHLIGHTED_SQUARE_COLOR =    Color(215, 110, 90)
        val DARK_HIGHLIGHTED_SQUARE_COLOR =     Color(225, 100, 80)
        val FONT_COLOR =                        Color(238, 238, 238)

        const val SELECTED_SQUARE_OUTLINE_WIDTH = 4f
    }

    constructor() : this(board = Board.startingPosition())

    init {
        background = BACKGROUND_COLOR
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2 = g as Graphics2D
        g2.stroke = BasicStroke(SELECTED_SQUARE_OUTLINE_WIDTH)

        g.font = Font("Arial", Font.PLAIN, 32)

        val boardLeftX = (width / 2) - (Gui.SQUARE_SIZE * 4)
        val boardTopY = (height / 2) - (Gui.SQUARE_SIZE * 4) + 32
        val textTopY = boardTopY - 64

        for (x in 0..7) {
            for (y in 0..7) {
                val isLight = (x + y) % 2 == 0
                val currentSquare = Square.of(x, y)

                val isSelected = arrayOf(
                    selectedSquare,
                    board.stack.lastOrNull()?.from,
                    board.stack.lastOrNull()?.to
                )
                    .contains(currentSquare)

                val isHighlighted = highlightedSquares.contains(currentSquare)

                g.color = if (isLight) {
                    when {
                        isHighlighted   -> LIGHT_HIGHLIGHTED_SQUARE_COLOR
                        isSelected      -> SELECTED_LIGHT_SQUARE_COLOR
                        else            -> LIGHT_SQUARE_COLOR
                    }
                } else {
                    when {
                        isHighlighted   -> DARK_HIGHLIGHTED_SQUARE_COLOR
                        isSelected      -> SELECTED_DARK_SQUARE_COLOR
                        else            -> DARK_SQUARE_COLOR
                    }
                }

                val xPos = boardLeftX + x * Gui.SQUARE_SIZE
                val yPos = boardTopY + y * Gui.SQUARE_SIZE

                g.fillRect(xPos, yPos,Gui.SQUARE_SIZE, Gui.SQUARE_SIZE)
                if (currentSquare == selectedSquare) {
                    g2.color = SELECTED_SQUARE_OUTLINE
                    val indent = (SELECTED_SQUARE_OUTLINE_WIDTH / 2).toInt()
                    g2.drawRect(
                        xPos + indent, yPos + indent,
                        // No idea why multiplication by two is necessary, just don't touch it
                        Gui.SQUARE_SIZE - indent * 2, Gui.SQUARE_SIZE - indent * 2)
                }

                val piece = board.data[x, y]

                if (piece != Piece.EMPTY) {
                    g.drawImage(pieceSpriteMap[piece], xPos, yPos,
                        Gui.SQUARE_SIZE, Gui.SQUARE_SIZE, this)
                }
            }
        }

        g.color = FONT_COLOR
        g.drawString("MorphyChess", boardLeftX, textTopY)

    }

    fun selectSquare(square: Square) {
        selectedSquare = square
        val moves = board.generateLegalMoves()
        highlightedSquares.clear()
        for (move in moves) {
            if (move.from == square) {
                highlightedSquares.add(move.to)
            }
        }
    }

    fun deselectSquares() {
        selectedSquare = null
        highlightedSquares.clear()
    }

    override fun mouseClicked(e: MouseEvent)  {
        requestFocusInWindow() // Re-focus panel to keep allowing keyboard events

        val boardLeftX = (width / 2) - (Gui.SQUARE_SIZE * 4)
        val boardTopY = (height / 2) - (Gui.SQUARE_SIZE * 4) + 32
        // Out of bounds check is integrated into the safe `Square.of()`
        val clickedSquare = Square.of(
            (e.x - boardLeftX) / (Gui.SQUARE_SIZE),
            (e.y - boardTopY) / (Gui.SQUARE_SIZE)
        )

        if (!clickedSquare.isValid) return // TODO: process gui buttons

        when (selectedSquare) {
            null -> {
                selectSquare(clickedSquare)
            }
            clickedSquare -> {
                deselectSquares()
            }
            else -> {
                var move: Move? = null

                for (candidateMove in board.generateLegalMoves()) {
                    if ((candidateMove.from == selectedSquare) and
                        (candidateMove.to == clickedSquare)) {
                        move = candidateMove
                        break
                    }
                }

                if (move != null) {
                    board.pushMove(move)
                    deselectSquares()
                } else {
                    selectSquare(clickedSquare)
                }

            }
        }

        this.repaint()
    }

    // Mouse events
    override fun mousePressed(e: MouseEvent)  { }
    override fun mouseReleased(e: MouseEvent) { }
    override fun mouseEntered(e: MouseEvent)  { }
    override fun mouseExited(e: MouseEvent)   { }
    override fun mouseDragged(e: MouseEvent?) { }

    override fun mouseMoved(e: MouseEvent?) { }


    // Keyboard events
    override fun keyPressed(e: KeyEvent?) {
        if (e == null) return

        when (e.keyChar) {
            'u' -> {
                // Undo move

                if (board.stack.isNotEmpty()) {
                    board.popMove()
                    repaint()
                }
            }
        }
    }

    override fun keyTyped(e: KeyEvent?) { }
    override fun keyReleased(e: KeyEvent?) { }
}


class Gui{

    companion object {
        const val SQUARE_SIZE = 60
    }


    init {
        val totalWidth: Int = SQUARE_SIZE * 12
        val totalHeight: Int = SQUARE_SIZE * 12

        val frame = JFrame("MorphyChess")
        val board = Board.startingPosition()
        val chessBoard = ChessBoard(board)

        frame.apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(totalWidth, totalHeight)
            add(chessBoard)
            isVisible = true
        }

        // Listeners must be attached to the frame, otherwise the coordinates gets messed up
        chessBoard.apply {
            isFocusable = true // To allow keyboard events
            addMouseListener(this)
            addMouseMotionListener(this)
            addKeyListener(this)
        }

        // Focus the board by default
        chessBoard.requestFocusInWindow()
    }
}


