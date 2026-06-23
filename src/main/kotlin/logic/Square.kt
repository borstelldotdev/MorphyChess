@file:Suppress("unused")

package main.logic

@JvmInline
value class Square(val value: Int) {
    // MSB <-   i   0       yyy             xxx         -> LSB
    //          invalid     y-coordinate    x-coordinate
    //

    val x:
            Int get() = value and 0b0_0_000_111
    val y:
            Int get() = (value and 0b0_0_111_000) shr 3
    val isValid:
            Boolean get() = (value and 0b1_0_000_000) == 0
    val isInvalid:
            Boolean get() = (value and 0b1_0_000_000) != 0

    fun offset(xOffset: Int, yOffset: Int): Square {
        return of(x + xOffset, y + yOffset)
    }

    fun xOffset(xOffset: Int): Square {
        return of(x + xOffset, y)
    }

    fun yOffset(yOffset: Int): Square {
        return of(x, y + yOffset)
    }

    override fun toString(): String =
        if (isValid) "Square(${"ABCDEFGH"[x]}${"87654321"[y]})" else "Square(Invalid)"

    companion object {
        fun of(x: Int, y: Int): Square {
            if (!(x in 0..7 && y in 0..7)) {
                return NONE
            }

            return Square((y shl 3) or x)
        }

        fun ofUnsafe(x: Int, y: Int): Square =
            Square((y shl 3) or x)


        fun fromString(string: String): Square = of(
            x = "ABCDEFGI".indexOf(string[0].lowercase()),
            y = "87654321".indexOf(string[0])
        )

        val NONE = Square(0x80)
        
        val A1 = of(0, 7)
        val A2 = of(0, 6)
        val A3 = of(0, 5)
        val A4 = of(0, 4)
        val A5 = of(0, 3)
        val A6 = of(0, 2)
        val A7 = of(0, 1)
        val A8 = of(0, 0)

        val B1 = of(1, 7)
        val B2 = of(1, 6)
        val B3 = of(1, 5)
        val B4 = of(1, 4)
        val B5 = of(1, 3)
        val B6 = of(1, 2)
        val B7 = of(1, 1)
        val B8 = of(1, 0)

        val C1 = of(2, 7)
        val C2 = of(2, 6)
        val C3 = of(2, 5)
        val C4 = of(2, 4)
        val C5 = of(2, 3)
        val C6 = of(2, 2)
        val C7 = of(2, 1)
        val C8 = of(2, 0)

        val D1 = of(3, 7)
        val D2 = of(3, 6)
        val D3 = of(3, 5)
        val D4 = of(3, 4)
        val D5 = of(3, 3)
        val D6 = of(3, 2)
        val D7 = of(3, 1)
        val D8 = of(3, 0)

        val E1 = of(4, 7)
        val E2 = of(4, 6)
        val E3 = of(4, 5)
        val E4 = of(4, 4)
        val E5 = of(4, 3)
        val E6 = of(4, 2)
        val E7 = of(4, 1)
        val E8 = of(4, 0)

        val F1 = of(5, 7)
        val F2 = of(5, 6)
        val F3 = of(5, 5)
        val F4 = of(5, 4)
        val F5 = of(5, 3)
        val F6 = of(5, 2)
        val F7 = of(5, 1)
        val F8 = of(5, 0)

        val G1 = of(6, 7)
        val G2 = of(6, 6)
        val G3 = of(6, 5)
        val G4 = of(6, 4)
        val G5 = of(6, 3)
        val G6 = of(6, 2)
        val G7 = of(6, 1)
        val G8 = of(6, 0)

        val H1 = of(7, 7)
        val H2 = of(7, 6)
        val H3 = of(7, 5)
        val H4 = of(7, 4)
        val H5 = of(7, 3)
        val H6 = of(7, 2)
        val H7 = of(7, 1)
        val H8 = of(7, 0)
    }
}