package com.testmynoetic.core.verify

import java.time.LocalTime
import java.time.temporal.ChronoUnit

val set03: Map<Int, () -> Any> = mapOf(
    1 to { 25 * 12 },
    2 to { 5 * 12 + 7 },
    3 to { generateSequence(2) { it * 2 }.elementAt(5) },
    4 to {
        // Orders from tallest to shortest that fit both clues.
        V.permutations(listOf("Sam", "Kim", "Lee")).single { o ->
            o.indexOf("Sam") < o.indexOf("Kim") && o.indexOf("Kim") < o.indexOf("Lee")
        }.last()
    },
    5 to { listOf(3700, 3800).minBy { Math.abs(it - 3748) } },
    6 to { (1..900).single { 3 * it + 300 == 900 } },
    7 to { val w = (1..72).single { 9 * it == 72 }; 2 * (9 + w) },
    8 to { V.choose(listOf(1, 5, 10, 25), 2).map { it.sum() }.toSet().size },
    9 to { (1..1000).first { it % 4 == 0 && it % 6 == 0 && it % 10 == 0 } },
    10 to {
        (0..27).flatMap { t -> (0..27).map { s -> t to s } }.single { (t, s) -> 3 * t == 27 && t + s == 15 }.second
    },
    11 to { ChronoUnit.MINUTES.between(LocalTime.of(8, 15), LocalTime.of(14, 50)) },
    12 to { listOf(12, 18, 9, 15).let { it.max() - it.min() } },
    13 to { (1..19 step 2).sum() },
    14 to { (1..72).single { it + (it + 1) + (it + 2) == 72 } },
    15 to { 90 - 35 },
    16 to {
        var posts = 0
        for (x in 0..12 step 3) for (y in 0..12 step 3) if (x == 0 || x == 12 || y == 0 || y == 12) posts++
        posts
    },
    17 to { (21..39).single { it % 5 == 3 && it % 4 == 1 } },
    18 to {
        fun swap(s: List<String>, i: Int, j: Int) = s.toMutableList().also { it[i - 1] = s[j - 1]; it[j - 1] = s[i - 1] }
        V.permutations(listOf("Amy", "Bo", "Cy", "Di")).single { start ->
            swap(swap(swap(start, 1, 2), 2, 4), 3, 4) == listOf("Amy", "Bo", "Cy", "Di")
        }[0]
    },
    19 to { (0..50).single { 36 + it == 3 * (8 + it) } },
    20 to {
        var ways = 0
        for (n in 0..6) for (d in 0..3) for (q in 0..1) if (5 * n + 10 * d + 25 * q == 30) ways++
        ways
    },
)
