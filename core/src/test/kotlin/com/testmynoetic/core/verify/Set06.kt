package com.testmynoetic.core.verify

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

val set06: Map<Int, () -> Any> = mapOf(
    1 to { 7000 + 600 + 5 },
    2 to { 3 * 7 + 4 },
    3 to { (1..6).map { it * it }[5] },
    4 to { listOf("H", "T").flatMap { c -> (1..6).map { c + it } }.size },
    5 to { (1..84).single { 4 * it == 84 } },
    6 to { val s = 36 / 4; V.cellArea(listOf(0 to 0, s to 0, s to s, 0 to s)) },
    7 to {
        val muffinCents = (1..1000).single { 4 * it == 3 * 200 }
        check(6 * muffinCents % 100 == 0)
        6 * muffinCents / 100
    },
    8 to { (1..30).count { it % 3 == 0 && it % 5 == 0 } },
    9 to { 1000 % 7 },
    10 to {
        (1..16).flatMap { w -> (1..16).map { l -> w to l } }
            .single { (w, l) -> 2 * (w + l) == 32 && l == w + 4 }.let { (w, l) -> w * l }
    },
    11 to { (0..100).single { (it + 5) * 2 == 36 } },
    12 to { var n = 0; for (s in 1..3) for (d in 1..4) for (f in 1..2) n++; n },
    13 to {
        listOf(3 to 8, 1 to 2, 5 to 12, 2 to 3, 3 to 5).maxBy { it.first.toDouble() / it.second }
            .let { "${it.first}/${it.second}" }
    },
    14 to {
        // Try mirror lines through the center every half degree; count those that map the corners onto themselves.
        val corners = listOf(1.0 to 1.0, -1.0 to 1.0, -1.0 to -1.0, 1.0 to -1.0)
        fun key(p: Pair<Double, Double>) = (p.first * 1000).roundToLong() to (p.second * 1000).roundToLong()
        val target = corners.map(::key).toSet()
        (0 until 360).count { twiceAngle ->
            val t = twiceAngle * PI / 180
            corners.map { (x, y) -> key((x * cos(t) + y * sin(t)) to (x * sin(t) - y * cos(t))) }.toSet() == target
        }
    },
    15 to { 5 * (1..100).single { ana -> 5 * ana - ana == 48 } },
    16 to { (2..1000).first { n -> listOf(2, 3, 4, 5, 6).all { n % it == 1 } } },
    17 to { V.choose((1..5).toList(), 3).size },
    18 to {
        var n = 0
        for (x in -1 until 9) for (y in -1 until 6) if (!(x in 0 until 8 && y in 0 until 5)) n++
        n
    },
    19 to { V.perimeter(listOf(0 to 0, 15 to 0, 15 to 1, 0 to 1)) },
    20 to {
        val people = listOf("Ana", "Ben", "Cara", "Dev")
        val fur = setOf("cat", "dog")
        V.permutations(listOf("cat", "dog", "fish", "bird")).filter { pets ->
            val pet = people.zip(pets).toMap()
            pet["Ana"] !in fur && pet["Ana"] != "bird" &&
                pet["Ben"] in fur && pet["Ben"] != "dog" &&
                pet["Cara"] != "bird"
        }.single().let { people[it.indexOf("bird")] }
    },
)
