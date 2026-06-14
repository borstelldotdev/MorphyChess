package main.logic

@JvmInline
value class Square(val value: Int) {
    // x011  x110
    // from  to

    val x:
            Int get() = value and 0x07
    val y:
            Int get() = (value and 0x70) shr 4

    fun offset(xOffset: Int, yOffset: Int): Square {
        return of(x + xOffset, y + yOffset)
    }

    fun xOffset(xOffset: Int): Square {
        return of(x + xOffset, y)
    }

    fun yOffset(yOffset: Int): Square {
        return of(x, y + yOffset)
    }

    companion object {
        fun of(x: Int, y: Int): Square = Square((y shl 4) or x)

        fun fromString(string: String): Square = Square.of(
            x = "ABCDEFGI".indexOf(string[0].lowercase()),
            y = "87654321".indexOf(string[0])
        )
    }
}