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
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

class ChessBoard(val board: Board) : JPanel(), MouseListener, MouseMotionListener {
    companion object {
        val pieceSpriteMap: Map<Piece, BufferedImage> = buildMap {
            for (player in Player.entries) {
                if (player == Player.NONE) continue

                for (type in PieceType.entries) {
                    val color = player.name.lowercase()
                    val piece = type.name.lowercase()
                    val img = ImageIO.read(object {}.javaClass.getResourceAsStream("image_assets/$piece-$color.png"))
                    put(Piece.of(type, player), img)
                }
            }
        }

        var selectedSquare: Square? = null
        var moveFromSquare: Square? = null
        var moveToSquare:   Square? = null

        val BACKGROUND_COLOR =              Color(48, 48, 48)
        val LIGHT_SQUARE_COLOR =            Color(235, 210, 175)
        val DARK_SQUARE_COLOR =             Color(185, 135, 100)
        val SELECTED_LIGHT_SQUARE_COLOR =   Color(245, 235, 115)
        val SELECTED_DARK_SQUARE_COLOR =    Color(220, 190, 75)
        val SELECTED_SQUARE_OUTLINE =       Color(80, 80, 80)
        const val SELECTED_SQUARE_OUTLINE_WIDTH = 4f
        val FONT_COLOR =                    Color(238, 238, 238)
        val ARROW_COLOR =                   Color(255, 0, 0, 192)
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

        val boardLeftX = (width / 2) - (Gui.SQUARE_SIZE * 4) + 32
        val boardTopY = (height / 2) - (Gui.SQUARE_SIZE * 4)
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

                g.color = if (isSelected) {
                    if (isLight) SELECTED_LIGHT_SQUARE_COLOR else SELECTED_DARK_SQUARE_COLOR
                } else {
                    if (isLight) LIGHT_SQUARE_COLOR else DARK_SQUARE_COLOR
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
        g.drawString("JacSchack", boardLeftX, textTopY)

    }

    override fun mouseClicked(e: MouseEvent)  {
        val boardLeftX = (width / 2) - (Gui.SQUARE_SIZE * 4) + 32
        val boardTopY = (height / 2) - (Gui.SQUARE_SIZE * 4)
        // Out of bounds check is integrated into the safe `Square.of()`
        val clickedSquare = Square.of(
            (e.x - boardLeftX) / (Gui.SQUARE_SIZE),
            (e.y - boardTopY) / (Gui.SQUARE_SIZE)
        )

        if (!clickedSquare.exists) return // TODO: process gui buttons

        when (selectedSquare) {
            null -> {
                selectedSquare = clickedSquare
            }
            clickedSquare -> {
                selectedSquare = null
            }
            else -> {
                val move = Move.of(selectedSquare!!, clickedSquare, SpecialMoveType.NONE)
                board.pushMove(move)
                // TODO(Perform legality check)

                val isLegal = true
                if (isLegal) {
                    selectedSquare = null
                    moveFromSquare = move.from
                    moveToSquare = move.to
                }

                // selectedSquare = clickedSquare

            }
        }

        this.repaint()
    }
    override fun mousePressed(e: MouseEvent)  { }
    override fun mouseReleased(e: MouseEvent) { }
    override fun mouseEntered(e: MouseEvent)  { }
    override fun mouseExited(e: MouseEvent)   { }
    override fun mouseDragged(e: MouseEvent?) { }

    override fun mouseMoved(e: MouseEvent?) {

    }
}


class Gui{

    companion object {
        const val SQUARE_SIZE = 60
    }


    init {
        val totalWidth: Int = SQUARE_SIZE * 12
        val totalHeight: Int = SQUARE_SIZE * 12

        val frame = JFrame("JacSchack")
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
            addMouseListener(this)
            addMouseMotionListener(this)
        }
    }
}


