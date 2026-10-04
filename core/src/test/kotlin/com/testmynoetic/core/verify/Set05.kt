package com.testmynoetic.core.verify

val set05: Map<Int, () -> Any> = mapOf(
    1 to { 400 - 150 + 50 },
    2 to { generateSequence(81) { it - 9 }.elementAt(4) },
    3 to { V.cellArea(listOf(0 to 0, 8 to 0, 8 to 5, 0 to 5)) },
    4 to { V.weekday("Friday", -3) },
    5 to { 3450 / 10 },
    6 to { V.before(16, 10, 95) },
    7 to { (0..11).single { g -> g + (g + 5) == 11 } + 5 },
    8 to { listOf("r1", "r2", "r3").flatMap { a -> listOf("s1", "s2", "s3", "s4").map { a + it } }.size },
    9 to { (1000..9999).first { V.digits(it).toSet().size == 4 } },
    10 to {
        val corners = (0..7).map { listOf(it and 1, (it shr 1) and 1, (it shr 2) and 1) }
        val edges = V.choose(corners, 2).count { (a, b) -> a.indices.count { a[it] != b[it] } == 1 }
        edges * 5
    },
    11 to { (0..50).single { 3 * it + 8 == 50 } },
    12 to { listOf(85, 90, 75, 90).let { check(it.sum() % it.size == 0); it.sum() / it.size } },
    13 to { (1..45).filter { it % 3 != 0 }.size },
    14 to { V.rectanglesInGrid(1, 4) },
    15 to { (0..20).single { both -> 13 + 11 - both == 20 } },
    16 to { (0..10).single { s -> 8 * s + 6 * (10 - s) == 68 } },
    17 to { (10..99).count { it / 10 > it % 10 } },
    18 to { val small = 20 / 4; 4 * (2 * small) },
    19 to {
        val open = BooleanArray(11)
        for (student in 1..10) for (locker in student..10 step student) open[locker] = !open[locker]
        open.count { it }
    },
    20 to { (0..50).single { big -> big - (50 - big) == 14 }.let { it * (50 - it) } },
)
