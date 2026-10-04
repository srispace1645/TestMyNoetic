package com.testmynoetic.core.verify

val set08: Map<Int, () -> Any> = mapOf(
    1 to { 15 * 4 * 25 },
    2 to { 3 * 4 },
    3 to { listOf(5, 11, 17, 29).let { s -> val step = s[1] - s[0]; check(s[2] + 2 * step == s[3]); s[2] + step } },
    4 to { V.weekday("Wednesday", 14) },
    5 to { (100..999).filter { it % 2 == 1 && V.digits(it).toSet().size == 3 }.max() },
    6 to { val w = 6; val l = 3 * w; 2 * (l + w) },
    7 to { (0..7).single { adults -> 5 * adults + 3 * (7 - adults) == 27 } },
    8 to { "${(1..6).count { it > 4 }}/6" },
    9 to { V.factors(36).size },
    10 to { 3 * (50 + 30 + 50 + 30) },
    11 to {
        (0..17).flatMap { c -> (0..17).map { t -> c to t } }
            .single { (c, t) -> 2 * c + t == 17 && c + 2 * t == 13 }.let { it.first + it.second }
    },
    12 to { listOf(12, 7, 15, 9, 20, 11, 8).sorted()[3] },
    13 to { val twoThirds = 36 / 3 * 2; twoThirds / 4 * 3 },
    14 to { V.cellArea(listOf(0 to 0, 2 to 0, 2 to 2, 4 to 2, 4 to 4, 6 to 4, 6 to 6, 0 to 6)) },
    15 to {
        V.permutations(listOf("Ana", "Ben", "Cy", "Dot")).filter { o ->
            val pos = o.withIndex().associate { it.value to it.index + 1 }
            pos["Ana"] == 3 && Math.abs(pos.getValue("Ben") - 3) == 1 &&
                pos["Cy"] in listOf(1, 4) && Math.abs(pos.getValue("Dot") - pos.getValue("Cy")) != 1
        }.map { it[1] }.toSet().single()
    },
    16 to { (0..120).single { s -> s + (s + 30) + (s + 60) == 120 } },
    17 to { (0..9).single { d -> (400 + 10 * d + 3) % 9 == 0 } },
    18 to {
        var n = 0
        for (x in 0..2) for (y in 0..2) for (z in 0..2) if (listOf(x, y, z).count { it == 0 || it == 2 } == 1) n++
        n
    },
    19 to {
        fun paths(r: Int, c: Int): Int = if (r == 0 || c == 0) 1 else paths(r - 1, c) + paths(r, c - 1)
        paths(2, 3)
    },
    20 to { (0 until 8).sumOf { 1 shl it } },
)
