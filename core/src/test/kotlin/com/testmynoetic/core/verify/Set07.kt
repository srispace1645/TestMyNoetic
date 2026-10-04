package com.testmynoetic.core.verify

import java.time.LocalTime
import java.time.temporal.ChronoUnit

val set07: Map<Int, () -> Any> = mapOf(
    1 to { 50 * 40 },
    2 to { generateSequence(5) { it + 5 }.elementAt(9) },
    3 to { 4 * 100 },
    4 to { val colors = listOf("r", "b", "g", "y"); colors.flatMap { a -> colors.map { a + it } }.size },
    5 to { 199 + 299 },
    6 to { ChronoUnit.MINUTES.between(LocalTime.of(14, 45), LocalTime.of(16, 20)) },
    7 to { listOf("circle", "triangle", "square", "square")[(30 - 1) % 4] },
    8 to { V.choose((1..6).toList(), 2).size },
    9 to { (10..99).filter { V.isPrime(it) }.let { it.first() * it.last() } },
    10 to { (15 + 10 + 15 + 10) * 3 },
    11 to {
        (0..650).flatMap { c -> (0..650).map { p -> c to p } }
            .single { (c, p) -> c + p == 500 && 2 * c + p == 650 }.second
    },
    12 to { (0..100).single { (15 + 22 + it) / 3.0 == 20.0 } },
    13 to { (101..199).count { it % 2 == 0 } },
    14 to { var n = 0; for (x in 0 until 4) for (y in 0 until 3) for (z in 0 until 2) n++; n },
    15 to { (1..30).single { fromBack -> 12 + fromBack - 1 == 30 } },
    16 to { 3 * (0..100).single { ann -> 3 * ann - 10 == ann + 10 } },
    17 to { (1..10000).first { V.digits(it).sum() == 25 } },
    18 to { (2 * 60 + 30) / 2 }, // the hour hand turns half a degree per minute
    19 to {
        (2..30).single { n ->
            var games = 0
            for (a in 1..n) for (b in a + 1..n) games++
            games == 28
        }
    },
    20 to { (0..30).single { m -> V.pyramidTop(listOf(5, m, 7)) == 30 } },
)
