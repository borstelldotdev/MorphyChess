package main.logic

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class BoardTest {
    @ParameterizedTest(name = "perft: {0} depth={1} expect={2}")
    @MethodSource("perftCases")
    fun testPerft(fen: String, depth: Int, expectedNodes: Int) {
        val board = Board.fromFen(fen)
        val actualNodes = board.perft(depth)
        assertEquals(expectedNodes, actualNodes,
            "Perft failed for fen `$fen`, expected $expectedNodes nodes, but got $actualNodes")
    }

    companion object {
        @JvmStatic
        fun perftCases() = listOf(
            // Startposition
            Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 1, 20),
            Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 2, 400),
            /*Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 3, 8902),
            Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 4, 197281),*/

            // https://gist.github.com/peterellisjones/8c46c28141c162d1d8a0f0badbc9cff9
            Arguments.of("r6r/1b2k1bq/8/8/7B/8/8/R3K2R b KQ - 3 2", 1, 8),
            Arguments.of("8/8/8/2k5/2pP4/8/B7/4K3 b - d3 0 3", 1, 8),
            Arguments.of("r1bqkbnr/pppppppp/n7/8/8/P7/1PPPPPPP/RNBQKBNR w KQkq - 2 2", 1, 19),
            Arguments.of("r3k2r/p1pp1pb1/bn2Qnp1/2qPN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQkq - 3 2", 1, 5),
            Arguments.of("2kr3r/p1ppqpb1/bn2Qnp1/3PN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQ - 3 2", 1, 44),
            Arguments.of("rnb2k1r/pp1Pbppp/2p5/q7/2B5/8/PPPQNnPP/RNB1K2R w KQ - 3 9", 1, 39),
            Arguments.of("2r5/3pk3/8/2P5/8/2K5/8/8 w - - 5 4", 1, 9),
            /*Arguments.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379),
            Arguments.of("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 3, 89890),
            Arguments.of("3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1", 6, 1134888),
            Arguments.of("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1", 6, 1015133),
            Arguments.of("8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1", 6, 1440467),
            Arguments.of("5k2/8/8/8/8/8/8/4K2R w K - 0 1", 6, 661072),
            Arguments.of("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1", 6, 803711),
            Arguments.of("r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1", 4, 1274206),
            Arguments.of("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1", 4, 1720476),
            Arguments.of("2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1", 6, 3821001),
            Arguments.of("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1", 5, 1004658),
            Arguments.of("4k3/1P6/8/8/8/8/K7/8 w - - 0 1", 6, 217342),
            Arguments.of("8/P1k5/K7/8/8/8/8/8 w - - 0 1", 6, 92683),
            Arguments.of("K1k5/8/P7/8/8/8/8/8 w - - 0 1", 6, 2217),
            Arguments.of("8/k1P5/8/1K6/8/8/8/8 w - - 0 1", 7, 567584),
            Arguments.of("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1", 4, 23527),*/

            // https://www.chessprogramming.org/Perft_Results
            Arguments.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 1, 48),
            /*Arguments.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 2, 2039),
            Arguments.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3, 97862),*/

            Arguments.of("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 1, 14),
            Arguments.of("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 2, 191),
            /*Arguments.of("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 3, 2812),
            Arguments.of("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 4, 43238),*/

            Arguments.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 1, 6),
            Arguments.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 2, 264),
            //Arguments.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3, 9467),

            Arguments.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 1, 44),
            Arguments.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 2, 1486),
            //Arguments.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379),
        )
    }
}