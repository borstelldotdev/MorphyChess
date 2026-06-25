package board

enum class Player(val value: Int) {
    WHITE(0x20),
    BLACK(0x10),
    NONE(0x0);

    fun opponent(): Player = when (this) {
        Player.WHITE -> Player.BLACK
        Player.BLACK -> Player.WHITE
        Player.NONE -> Player.NONE
    }

    override fun toString(): String =
        when (this) {
            Player.WHITE -> "Player(White)"
            Player.BLACK -> "Player(Black)"
            Player.NONE -> "Player(None)"
        }
}