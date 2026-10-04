package com.testmynoetic.core.verify

val set01: Map<Int, () -> Any> = mapOf(
    1 to { 125 + 375 + 500 },
    2 to { 3 * 16 + 4 },
    3 to { 2 * 9 + 2 * 6 },
    4 to { generateSequence(3) { it + 4 }.elementAt(5) },
    5 to { V.weekday("Monday", 10) },
    6 to { 3 * 25 + 4 * 10 + 6 * 5 - 95 },
    7 to { (1..26).single { pie -> 2 * pie + 8 == 26 } },
    8 to { V.after(13, 50, 60 + 45) },
    9 to { (10..99).count { V.digits(it).sum() == 5 } },
    10 to { (100..999).last { it % 8 == 0 } },
    11 to { V.squaresInGrid(3, 3) },
    12 to { V.pyramidTop(listOf(3, 5, 2, 6)) },
    13 to { V.permutations(listOf("Ana", "Ben", "Cara", "Dev")).count { it.first() == "Ana" } },
    14 to { (10..99).single { it % 7 == 0 && it % 10 == it / 10 + 2 } },
    15 to { 3 * (0..48).single { zara -> zara + 3 * zara == 48 } },
    16 to { V.cellArea(listOf(0 to 0, 8 to 0, 8 to 4, 5 to 4, 5 to 6, 0 to 6)) },
    17 to {
        // Largest handful with no color 3 times, plus one more.
        listOf(6, 5, 4).sumOf { minOf(it, 2) } + 1
    },
    18 to {
        V.permutations(listOf(2, 4, 6, 8)).minOf { (a, b, c, d) -> Math.abs((10 * a + b) - (10 * c + d)) }
    },
    19 to { (4..60).single { owen -> (owen + 3) + (owen - 4 + 3) == 30 } },
    20 to {
        val kids = 1..6
        kids.sumOf { giver -> kids.count { it != giver } }
    },
)
