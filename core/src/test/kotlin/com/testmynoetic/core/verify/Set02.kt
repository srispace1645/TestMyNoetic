package com.testmynoetic.core.verify

val set02: Map<Int, () -> Any> = mapOf(
    1 to { 1000 - 368 },
    2 to { (1..20).single { 7 * it == 56 } },
    3 to { 7 + 7 + 7 + 7 },
    4 to { listOf("t1", "t2", "t3").flatMap { t -> listOf("s1", "s2").map { t + it } }.size },
    5 to { 3 * 60 + 20 },
    6 to { (1..100).single { n -> n - 9 + 1 == 7 } },
    7 to { (1..10).first { it * 36 >= 150 } },
    8 to {
        val rows = listOf(2 to 7, 3 to 10, 5 to 16)
        val (a, b) = (0..10).flatMap { a -> (-10..10).map { b -> a to b } }
            .single { (a, b) -> rows.all { (x, y) -> a * x + b == y } }
        a * 8 + b
    },
    9 to { 360 / 12 * 4 },
    10 to {
        // Build the class: students 0-4 have both, 5-13 dog only, 14-17 cat only.
        val dog = (0 until 14).toSet()
        val cat = (0 until 5).toSet() + (14 until 18)
        check(dog.size == 14 && cat.size == 9 && (dog intersect cat).size == 5)
        (0 until 25).count { it !in dog && it !in cat }
    },
    11 to { (21..39).count { V.isPrime(it) } },
    12 to { (0 until 5).sumOf { 10 + 6 * it } },
    13 to { V.choose((1..5).toList(), 2).size },
    14 to { (0..12).single { trikes -> 3 * trikes + 2 * (12 - trikes) == 29 } },
    15 to { (0..9).single { d -> (10 * d + 8) + (40 + d) == 114 } },
    16 to { (1..48).single { 8 * it == 48 }.let { 3 * it * it } },
    17 to { (1..60).sumOf { it.toString().length } },
    18 to {
        V.permutations(listOf("Ava", "Ben", "Cal", "Dee", "Eli")).filter { o ->
            o.indexOf("Ava") < o.indexOf("Ben") && o.indexOf("Ben") < o.indexOf("Dee") &&
                o.indexOf("Cal") == o.indexOf("Dee") + 1 && o.last() == "Eli"
        }.single()[2]
    },
    19 to {
        var count = 0
        for (x in 0..3) for (y in 0..3) for (z in 0..3) if (listOf(x, y, z).all { it in 1..2 }) count++
        count
    },
    20 to {
        (1..500).single { start ->
            if (start % 2 != 0) return@single false
            val afterEat = start / 2 - 4
            afterEat > 0 && afterEat % 2 == 0 && afterEat / 2 == 9
        }
    },
)
