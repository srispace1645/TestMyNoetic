package com.testmynoetic.core.verify

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

val set10: Map<Int, () -> Any> = mapOf(
    1 to { 2026 - 26 },
    2 to { 2 * 3 * 12 },
    3 to { generateSequence(2) { it + 3 }.elementAt(4) },
    4 to { V.subsets(listOf(1, 2, 3)).size }, // each flip is heads (in the subset) or tails
    5 to { 6 + 4 * 5 },
    6 to {
        (1..14).flatMap { w -> (w..14).map { l -> w to l } }
            .single { (w, l) -> l + w == 14 && l * w == 48 }.second
    },
    7 to { (0..140).single { g -> g + (g + 20) == 140 } },
    8 to {
        var teams = 8; var games = 0
        while (teams > 1) { games += teams / 2; teams -= teams / 2 }
        games
    },
    9 to { 6 * 98 },
    10 to { ChronoUnit.MINUTES.between(LocalDateTime.of(2026, 11, 3, 22, 40), LocalDateTime.of(2026, 11, 4, 1, 15)) },
    11 to {
        // Find whole-number weights that fit, then compare.
        val (p, a, m) = (1..20).flatMap { p -> (1..20).flatMap { a -> (1..20).map { m -> Triple(p, a, m) } } }
            .first { (p, a, m) -> 2 * p == 6 * a && a == 2 * m }
        check((3 * p) % m == 0 && a > 0)
        3 * p / m
    },
    12 to { (0..100).single { 4 * 88 + it == 5 * 90 } },
    13 to { V.after(8, 0, (1..1000).first { it % 12 == 0 && it % 18 == 0 }) },
    14 to { 3 * V.perimeter(listOf(1 to 0, 2 to 0, 2 to 1, 3 to 1, 3 to 2, 2 to 2, 2 to 3, 1 to 3, 1 to 2, 0 to 2, 0 to 1, 1 to 1)) },
    15 to { (100..999).count { V.digits(it).sum() == 4 } },
    16 to { (1..50).sum() },
    17 to { (5..100).single { t -> t + 5 == 2 * (t - 4) } },
    18 to {
        // Count the outside unit faces of a 5 x 4 x 3 block of cubes: bottom and four sides, no top.
        var faces = 0
        for (x in 0 until 5) for (y in 0 until 4) for (z in 0 until 3) {
            if (z == 0) faces++
            if (x == 0) faces++
            if (x == 4) faces++
            if (y == 0) faces++
            if (y == 3) faces++
        }
        faces
    },
    19 to { (1..100).first { kids -> kids > 2 * 12 } },
    20 to { var n = 0; for (row in 1..12) for (col in 1..row) n++; n },
)
