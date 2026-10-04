package com.testmynoetic.core.verify

val set09: Map<Int, () -> Any> = mapOf(
    1 to { 1234 + 4321 },
    2 to { (1..45).single { 45 % it == 0 && 45 / it == 9 } },
    3 to { 2 * 24 },
    4 to {
        V.permutations(listOf("Ivy", "Jo", "Kai", "Lu")).single { o ->
            o.indexOf("Ivy") < o.indexOf("Jo") && o.indexOf("Jo") < o.indexOf("Kai") && o.indexOf("Kai") < o.indexOf("Lu")
        }[1]
    },
    5 to { check(1000 % 8 == 0); 1000 / 8 },
    6 to { (1..32).single { 4 * it == 10 + 6 + 10 + 6 } },
    7 to { generateSequence(1 to 1) { (value, step) -> value + step to step + 1 }.elementAt(6).first },
    8 to { val votes = listOf(8, 12, 6, 10); "${votes[1]}/${votes.sum()}" },
    9 to { (0..5).first { (247 + it) % 6 == 0 } },
    10 to { 15 * 60 },
    11 to { (1..100).single { day -> 10 + 3 * (day - 1) == 40 } },
    12 to {
        // Each lunch: a soup or nothing, and a sandwich or nothing, but not nothing at all.
        val soups = listOf(null, "s1", "s2", "s3")
        val sandwiches = listOf(null, "w1", "w2", "w3", "w4")
        soups.flatMap { s -> sandwiches.map { w -> s to w } }.count { it.first != null || it.second != null }
    },
    13 to { "0.${345 - 280}" },
    14 to { 360 / 8 },
    15 to { (1..6).sumOf { r -> (1..6).count { b -> r + b == 7 } } },
    16 to {
        (1..500).single { start ->
            if (start % 2 != 0) return@single false
            val noon = start - (start / 2 + 2)
            noon > 0 && noon % 2 == 0 && noon - (noon / 2 + 2) == 4
        }
    },
    17 to { (1..50).sumOf { n -> n.toString().count { it == '3' } } },
    18 to { val side = (1..8).last { 12 % it == 0 && 8 % it == 0 }; (12 / side) * (8 / side) },
    19 to {
        V.permutations(listOf("Ana", "Ben", "Cy", "Dee", "Eve")).count { Math.abs(it.indexOf("Ana") - it.indexOf("Ben")) == 1 }
    },
    20 to { (1..40).single { minutes -> 3 * minutes + 5 * minutes == 40 } },
)
