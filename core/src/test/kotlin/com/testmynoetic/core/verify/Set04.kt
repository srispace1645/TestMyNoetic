package com.testmynoetic.core.verify

val set04: Map<Int, () -> Any> = mapOf(
    1 to { 48 + 37 + 52 + 63 },
    2 to { 3 * 3 + 2 * 4 },
    3 to { (0..64).single { 64 - it == 27 } },
    4 to { 9 - 4 },
    5 to { 6 * 7 * 5 },
    6 to { 120 + 80 + 120 + 80 },
    7 to { (0..12).single { 4 * 7 + 3 * it == 37 } },
    8 to { "4/10" },
    9 to { (1..24).last { 24 % it == 0 && 36 % it == 0 } },
    10 to { val apart = Math.abs(5 * 30 - 0); minOf(apart, 360 - apart) },
    11 to { "FALL".repeat(20)[49].toString() },
    12 to { V.permutations(listOf("A", "B", "C", "D")).map { it.take(3) }.toSet().size },
    13 to { "${8 - 2 - 3}/8" },
    14 to { var n = 0; for (x in 0 until 10 step 2) for (y in 0 until 8 step 2) n++; n },
    15 to { (0..100).first { 10 + 5 * it == 30 + 3 * it } },
    16 to {
        val kids = listOf("Alex", "Bree", "Cole")
        kids.single { eater ->
            listOf(eater == "Alex", eater != "Alex", eater != "Cole").count { it } == 1
        }
    },
    17 to { (1..100).count { it % 3 == 0 || it % 5 == 0 } },
    18 to { V.perimeter(listOf(0 to 0, 12 to 0, 12 to 4, 8 to 4, 8 to 8, 0 to 8)) },
    19 to {
        // Every unit edge of every square, counting shared edges once.
        val edges = mutableSetOf<List<Int>>()
        for (i in 0 until 25) {
            edges += listOf(i, 0, i + 1, 0); edges += listOf(i, 1, i + 1, 1)
            edges += listOf(i, 0, i, 1); edges += listOf(i + 1, 0, i + 1, 1)
        }
        edges.size
    },
    20 to {
        // Breadth-first search over how many sides of each pancake are cooked.
        var frontier = setOf(listOf(0, 0, 0))
        var minutes = 0
        while (listOf(2, 2, 2) !in frontier) {
            minutes++
            frontier = frontier.flatMap { s ->
                val open = s.indices.filter { s[it] < 2 }
                val picks = V.subsets(open).filter { it.size in 1..2 }
                picks.map { p -> s.mapIndexed { i, v -> if (i in p) v + 1 else v } }
            }.toSet()
        }
        minutes
    },
)
