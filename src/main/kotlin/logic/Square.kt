package main.logic

@JvmInline
value class Square(val value: Int) {
    // MSB <-   i   0       ttt     fff -> LSB
    //          invalid     to      from
    //
    // TODO: use first 6 bits directly, instead of unpacking to then recombine

    val x:
            Int get() = value and 0b0_0_000_111
    val y:
            Int get() = (value and 0b0_0_111_000) shr 3
    val exists:
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
        if (exists) "Square(${"ABCDEFGH"[x]}${"87654321"[y]})" else "Square(Invalid)"

    companion object {
        fun of(x: Int, y: Int): Square {
            if (!(x in 0..7 && y in 0..7)) {
                return NONE
            }

            return Square((y shl 3) or x or 0x80)
        }

        fun ofUnsafe(x: Int, y: Int): Square =
            Square((y shl 3) or x or 0x80)


        fun fromString(string: String): Square = Square.of(
            x = "ABCDEFGI".indexOf(string[0].lowercase()),
            y = "87654321".indexOf(string[0])
        )

        val NONE = Square(0)
    }
}