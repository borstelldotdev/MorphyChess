@file:Suppress("SpellCheckingInspection")

package main.tests

import board.Board

class PerftTestCase(val fen: String, val depth: Int, val expectedNodes: Int) : AbstractTestCase {
    override fun runTestCase(): Boolean {
        val board = Board.fromFen(fen)
        val accualNodes = board.perft(depth)

        if (expectedNodes != accualNodes) {
            val board = Board.fromFen(fen)
            println("Perft for $fen failed: expectedNodes: $expectedNodes, " +
                    "accualNodes: $accualNodes, depth: $depth")
            board.perftVerbose(depth)
            return false
        }
        return true
    }
}

class PerftTest : AbstractTest {
    override fun generateTestCases(): List<PerftTestCase> {
        return listOf(
            // Starting position
            PerftTestCase("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 1, 20),
            PerftTestCase("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 2, 400),
            /*PerftTestCase("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 3, 8902),
            PerftTestCase("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 4, 197281),*/

            // https://gist.github.com/peterellisjones/8c46c28141c162d1d8a0f0badbc9cff9
            PerftTestCase("r6r/1b2k1bq/8/8/7B/8/8/R3K2R b KQ - 3 2", 1, 8),
            PerftTestCase("8/8/8/2k5/2pP4/8/B7/4K3 b - d3 0 3", 1, 8),
            PerftTestCase("r1bqkbnr/pppppppp/n7/8/8/P7/1PPPPPPP/RNBQKBNR w KQkq - 2 2", 1, 19),
            PerftTestCase("r3k2r/p1pp1pb1/bn2Qnp1/2qPN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQkq - 3 2", 1, 5),
            PerftTestCase("2kr3r/p1ppqpb1/bn2Qnp1/3PN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQ - 3 2", 1, 44),
            PerftTestCase("rnb2k1r/pp1Pbppp/2p5/q7/2B5/8/PPPQNnPP/RNB1K2R w KQ - 3 9", 1, 39),
            PerftTestCase("2r5/3pk3/8/2P5/8/2K5/8/8 w - - 5 4", 1, 9),
            /*PerftTestCase("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379),
            PerftTestCase("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 3, 89890),
            PerftTestCase("3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1", 6, 1134888),
            PerftTestCase("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1", 6, 1015133),
            PerftTestCase("8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1", 6, 1440467),
            PerftTestCase("5k2/8/8/8/8/8/8/4K2R w K - 0 1", 6, 661072),
            PerftTestCase("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1", 6, 803711),
            PerftTestCase("r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1", 4, 1274206),
            PerftTestCase("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1", 4, 1720476),
            PerftTestCase("2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1", 6, 3821001),
            PerftTestCase("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1", 5, 1004658),
            PerftTestCase("4k3/1P6/8/8/8/8/K7/8 w - - 0 1", 6, 217342),
            PerftTestCase("8/P1k5/K7/8/8/8/8/8 w - - 0 1", 6, 92683),
            PerftTestCase("K1k5/8/P7/8/8/8/8/8 w - - 0 1", 6, 2217),
            PerftTestCase("8/k1P5/8/1K6/8/8/8/8 w - - 0 1", 7, 567584),
            PerftTestCase("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1", 4, 23527),*/

            // https://www.chessprogramming.org/Perft_Results
            PerftTestCase("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 1, 48),
            /*PerftTestCase("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 2, 2039),
            PerftTestCase("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3, 97862),*/

            PerftTestCase("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 1, 14),
            PerftTestCase("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 2, 191),
            /*PerftTestCase("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 3, 2812),
            PerftTestCase("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1 ", 4, 43238),*/

            PerftTestCase("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 1, 6),
            PerftTestCase("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 2, 264),
            //PerftTestCase("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3, 9467),

            PerftTestCase("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 1, 44),
            PerftTestCase("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 2, 1486),
            //PerftTestCase("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379),
        )
    }
}