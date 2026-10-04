package com.testmynoetic.core

import kotlin.random.Random

/** A generated question plus the numbers it was built from, so tests can re-check the answer. */
data class Drill(val kind: DrillKind, val question: Question, val params: Map<String, Int>)

enum class DrillKind(val label: String, val topic: Topic) {
    MULTIPLY("Multiplying", Topic.NUMBER),
    REMAINDER("Division with remainders", Topic.NUMBER),
    FACTORS("Counting factors", Topic.NUMBER),
    ROUNDING("Rounding", Topic.NUMBER),
    FRACTION_OF("Fraction of a number", Topic.NUMBER),
    NTH_TERM("Number patterns", Topic.ALGEBRA),
    SUM_DIFF("Sum and difference", Topic.ALGEBRA),
    HEADS_LEGS("Heads and legs", Topic.ALGEBRA),
    PERIMETER_AREA("Perimeter and area", Topic.GEOMETRY),
    ELAPSED("Elapsed time", Topic.GEOMETRY),
    CONVERT("Unit conversion", Topic.GEOMETRY),
    WEEKDAY("Calendar jumps", Topic.LOGIC),
    MULTIPLES("Counting multiples", Topic.LOGIC),
    AVERAGE("Averages", Topic.LOGIC),
}

/** Endless practice questions. The same kind and seed always give the same question. */
object DrillGenerator {

    val DAYS = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    private val NAMES = listOf(
        "Ava", "Leo", "Maya", "Noah", "Priya", "Ethan", "Zara", "Owen", "Lily", "Arjun",
        "Mia", "Caleb", "Sofia", "Ravi", "Emma", "Jack", "Nina", "Theo", "Aisha", "Ben",
    )

    fun random(seed: Long, kinds: List<DrillKind> = DrillKind.entries): Drill {
        val rnd = Random(seed)
        return make(kinds[rnd.nextInt(kinds.size)], rnd.nextLong())
    }

    fun make(kind: DrillKind, seed: Long): Drill {
        val r = Random(seed)
        val id = "drill-${kind.name.lowercase()}-$seed"
        val name = NAMES[r.nextInt(NAMES.size)]
        fun q(prompt: String, answer: Any, hint: String, solution: String, unit: String = "", unitBefore: String = "", kindOf: AnswerKind = AnswerKind.NUMBER) =
            Question(
                id = id, topic = kind.topic, skill = kind.label, difficulty = 1, prompt = prompt,
                answer = answer.toString(), kind = kindOf, unit = unit, unitBefore = unitBefore,
                hint = hint, solution = solution,
            )

        return when (kind) {
            DrillKind.MULTIPLY -> {
                val a = r.nextInt(12, 400)
                val b = r.nextInt(3, 30)
                Drill(kind, q(
                    "What is $a × $b?", a * b,
                    "Break $b into tens and ones, multiply each part, then add.",
                    splitMultiply(a, b),
                ), mapOf("a" to a, "b" to b))
            }
            DrillKind.REMAINDER -> {
                val d = r.nextInt(3, 10)
                val n = r.nextInt(30, 500).let { if (it % d == 0) it + 1 + r.nextInt(d - 1) else it }
                val quotient = n / d
                Drill(kind, q(
                    "$name has $n stickers and puts them into packs of $d. How many stickers are left over after making as many full packs as possible?",
                    n % d,
                    "Find the biggest multiple of $d that is not more than $n.",
                    "$quotient packs use $quotient × $d = ${quotient * d} stickers. $n − ${quotient * d} = ${n % d} left over.",
                    unit = "stickers",
                ), mapOf("n" to n, "d" to d))
            }
            DrillKind.FACTORS -> {
                val n = listOf(12, 16, 18, 20, 24, 28, 30, 32, 36, 40, 42, 45, 48, 50, 54, 56, 60, 63, 64, 72, 75, 80, 81, 84, 90, 96, 98, 100)[r.nextInt(28)]
                val factors = (1..n).filter { n % it == 0 }
                val pairs = factors.filter { it * it <= n }.joinToString(", ") { "$it × ${n / it}" }
                Drill(kind, q(
                    "How many factors does $n have? (Count 1 and $n too.)", factors.size,
                    "List factor pairs that multiply to $n: 1 × $n, 2 × ?, …",
                    "Factor pairs: $pairs. That gives ${factors.joinToString(", ")}, which is ${factors.size} factors.",
                    unit = "factors",
                ), mapOf("n" to n))
            }
            DrillKind.ROUNDING -> {
                val n = r.nextInt(1000, 99999)
                val place = if (r.nextBoolean()) 100 else 1000
                val rounded = (n + place / 2) / place * place
                val word = if (place == 100) "hundred" else "thousand"
                Drill(kind, q(
                    "Round $n to the nearest $word.", rounded,
                    "Look at the digit just to the right of the $word place. 5 or more rounds up.",
                    "$n is between ${n / place * place} and ${n / place * place + place}. It is closer to $rounded, so it rounds to $rounded.",
                ), mapOf("n" to n, "place" to place))
            }
            DrillKind.FRACTION_OF -> {
                val den = listOf(2, 3, 4, 5, 6, 8, 10)[r.nextInt(7)]
                val num = r.nextInt(1, den)
                val whole = den * r.nextInt(3, 15)
                val part = whole / den * num
                Drill(kind, q(
                    "$name picked $whole apples and gave away $num/$den of them. How many apples did $name give away?", part,
                    "First find 1/$den of $whole by dividing.",
                    "1/$den of $whole is $whole ÷ $den = ${whole / den}. So $num/$den is $num × ${whole / den} = $part.",
                    unit = "apples",
                ), mapOf("num" to num, "den" to den, "whole" to whole))
            }
            DrillKind.NTH_TERM -> {
                val start = r.nextInt(1, 20)
                val step = r.nextInt(2, 12)
                val n = r.nextInt(10, 40)
                val shown = (0 until 4).joinToString(", ") { "${start + it * step}" }
                Drill(kind, q(
                    "What is the ${ordinal(n)} number in this pattern? $shown, …", start + (n - 1) * step,
                    "From the 1st number to the ${ordinal(n)}, you add $step a total of ${n - 1} times.",
                    "The pattern adds $step each time. ${ordinal(n)} number = $start + ${n - 1} × $step = ${start + (n - 1) * step}.",
                ), mapOf("start" to start, "step" to step, "n" to n))
            }
            DrillKind.SUM_DIFF -> {
                val small = r.nextInt(5, 60)
                val diff = r.nextInt(2, 30)
                val big = small + diff
                Drill(kind, q(
                    "$name and a friend have ${small + big} marbles together. $name has $diff more than the friend. How many marbles does $name have?", big,
                    "Take away the extra $diff first, then split the rest equally.",
                    "${small + big} − $diff = ${2 * small}. Half of that is $small, the friend's share. $name has $small + $diff = $big.",
                    unit = "marbles",
                ), mapOf("total" to small + big, "diff" to diff))
            }
            DrillKind.HEADS_LEGS -> {
                val chickens = r.nextInt(2, 20)
                val cows = r.nextInt(2, 20)
                val heads = chickens + cows
                val legs = 2 * chickens + 4 * cows
                Drill(kind, q(
                    "A farm has chickens and cows. There are $heads heads and $legs legs. How many cows are there?", cows,
                    "Pretend every animal is a chicken first. How many legs are missing?",
                    "If all $heads were chickens there would be ${2 * heads} legs. The extra $legs − ${2 * heads} = ${legs - 2 * heads} legs come from cows, 2 extra each, so ${legs - 2 * heads} ÷ 2 = $cows cows.",
                    unit = "cows",
                ), mapOf("heads" to heads, "legs" to legs))
            }
            DrillKind.PERIMETER_AREA -> {
                val w = r.nextInt(2, 15)
                val l = w + r.nextInt(1, 12)
                val p = 2 * (l + w)
                Drill(kind, q(
                    "A rectangle has a perimeter of $p cm. Its width is $w cm. What is its area?", l * w,
                    "Perimeter = 2 widths + 2 lengths. Find the length first.",
                    "Two widths are ${2 * w} cm, so two lengths are $p − ${2 * w} = ${p - 2 * w} cm and one length is $l cm. Area = $l × $w = ${l * w} sq cm.",
                    unit = "sq cm",
                ), mapOf("p" to p, "w" to w))
            }
            DrillKind.ELAPSED -> {
                val start = r.nextInt(8 * 12, 18 * 12) * 5
                val length = r.nextInt(7, 60) * 5
                val end = start + length
                Drill(kind, q(
                    "A football game started at ${clock(start)} and ended at ${clock(end)}. How many minutes did it last?", length,
                    "Count up to the next full hour, then add the rest.",
                    "From ${clock(start)} to ${clock(end)} is ${length / 60} hours ${length % 60} minutes = $length minutes.",
                    unit = "minutes",
                ), mapOf("start" to start, "end" to end))
            }
            DrillKind.CONVERT -> {
                val (one, big, small, factor) = listOf(
                    listOf("foot", "feet", "inches", "12"), listOf("yard", "yards", "feet", "3"),
                    listOf("hour", "hours", "minutes", "60"), listOf("pound", "pounds", "ounces", "16"),
                    listOf("gallon", "gallons", "quarts", "4"), listOf("meter", "meters", "centimeters", "100"),
                )[r.nextInt(6)]
                val f = factor.toInt()
                val bigCount = r.nextInt(2, 12)
                val extra = r.nextInt(1, f)
                Drill(kind, q(
                    "How many $small are in $bigCount $big and $extra $small?", bigCount * f + extra,
                    "1 $one = $f $small.",
                    "$bigCount × $f = ${bigCount * f} $small, plus $extra more is ${bigCount * f + extra} $small.",
                    unit = small,
                ), mapOf("big" to bigCount, "extra" to extra, "factor" to f))
            }
            DrillKind.WEEKDAY -> {
                val today = r.nextInt(7)
                val jump = r.nextInt(8, 120)
                val answer = DAYS[(today + jump) % 7]
                Drill(kind, q(
                    "Today is ${DAYS[today]}. What day of the week will it be $jump days from today?", answer,
                    "Every 7 days it's ${DAYS[today]} again. Find the remainder of $jump ÷ 7.",
                    "$jump = ${jump / 7} × 7 + ${jump % 7}. After ${jump / 7} full weeks it's ${DAYS[today]} again, then ${jump % 7} more days is $answer.",
                    kindOf = AnswerKind.WORD,
                ).copy(accept = listOf(answer.take(3))), mapOf("today" to today, "jump" to jump))
            }
            DrillKind.MULTIPLES -> {
                val m = r.nextInt(3, 13)
                val top = r.nextInt(5, 21) * 10
                Drill(kind, q(
                    "How many whole numbers from 1 to $top are multiples of $m?", top / m,
                    "Divide $top by $m and ignore the remainder.",
                    "$top ÷ $m = ${top / m} remainder ${top % m}. The multiples are $m, ${2 * m}, …, ${top / m * m}, which is ${top / m} numbers.",
                    unit = "numbers",
                ), mapOf("m" to m, "top" to top))
            }
            DrillKind.AVERAGE -> {
                val count = r.nextInt(3, 6)
                val mean = r.nextInt(60, 95)
                val scores = MutableList(count) { mean }
                repeat(count) {
                    val i = r.nextInt(count)
                    val j = r.nextInt(count)
                    val shift = r.nextInt(0, 6)
                    if (i != j && scores[j] - shift >= 50 && scores[i] + shift <= 100) {
                        scores[i] += shift; scores[j] -= shift
                    }
                }
                Drill(kind, q(
                    "$name's quiz scores were ${scores.joinToString(", ")}. What is the average (mean) score?", mean,
                    "Add all the scores, then divide by how many there are.",
                    "${scores.joinToString(" + ")} = ${scores.sum()}. ${scores.sum()} ÷ $count = $mean.",
                ), scores.withIndex().associate { "s${it.index}" to it.value } + ("count" to count))
            }
        }
    }

    fun ordinal(n: Int): String {
        val suffix = if (n % 100 in 11..13) "th" else when (n % 10) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }
        return "$n$suffix"
    }

    /** Minutes after midnight to "3:05 PM". */
    fun clock(minutes: Int): String {
        val h24 = (minutes / 60) % 24
        val m = minutes % 60
        val h12 = if (h24 % 12 == 0) 12 else h24 % 12
        return "$h12:${m.toString().padStart(2, '0')} ${if (h24 < 12) "AM" else "PM"}"
    }

    private fun splitMultiply(a: Int, b: Int): String {
        val tens = b / 10 * 10
        val ones = b % 10
        return when {
            tens == 0 -> "$a × $b = ${a * b}."
            ones == 0 -> "$a × $tens = ${a * tens}."
            else -> "$a × $tens = ${a * tens} and $a × $ones = ${a * ones}. ${a * tens} + ${a * ones} = ${a * b}."
        }
    }
}
