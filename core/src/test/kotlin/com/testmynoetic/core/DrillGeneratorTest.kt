package com.testmynoetic.core

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Re-checks every drill answer from its raw numbers, using a different method where possible. */
class DrillGeneratorTest {

    private fun expected(d: Drill): String {
        val p = d.params
        return when (d.kind) {
            DrillKind.MULTIPLY -> (p.getValue("a").toLong() * p.getValue("b")).toString()
            DrillKind.REMAINDER -> {
                var left = p.getValue("n")
                while (left >= p.getValue("d")) left -= p.getValue("d")
                left.toString()
            }
            DrillKind.FACTORS -> {
                // Count by prime factorization instead of trial division.
                var n = p.getValue("n"); var count = 1; var f = 2
                while (n > 1) { var e = 0; while (n % f == 0) { n /= f; e++ }; count *= e + 1; f++ }
                count.toString()
            }
            DrillKind.ROUNDING -> Math.round(p.getValue("n").toDouble() / p.getValue("place")).times(p.getValue("place")).toString()
            DrillKind.FRACTION_OF -> (p.getValue("whole") * p.getValue("num") / p.getValue("den")).toString()
            DrillKind.NTH_TERM -> generateSequence(p.getValue("start")) { it + p.getValue("step") }.elementAt(p.getValue("n") - 1).toString()
            DrillKind.SUM_DIFF -> ((p.getValue("total") + p.getValue("diff")) / 2).toString()
            DrillKind.HEADS_LEGS -> (0..p.getValue("heads")).single { cows -> 4 * cows + 2 * (p.getValue("heads") - cows) == p.getValue("legs") }.toString()
            DrillKind.PERIMETER_AREA -> (p.getValue("w") * (p.getValue("p") / 2 - p.getValue("w"))).toString()
            DrillKind.ELAPSED -> ChronoUnit.MINUTES.between(
                LocalTime.of(0, 0).plusMinutes(p.getValue("start").toLong()),
                LocalTime.of(0, 0).plusMinutes(p.getValue("end").toLong()),
            ).toString()
            DrillKind.CONVERT -> (p.getValue("big") * p.getValue("factor") + p.getValue("extra")).toString()
            DrillKind.WEEKDAY -> java.time.DayOfWeek.SUNDAY.plus((p.getValue("today") + p.getValue("jump")).toLong())
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.US)
            DrillKind.MULTIPLES -> (1..p.getValue("top")).count { it % p.getValue("m") == 0 }.toString()
            DrillKind.AVERAGE -> {
                val scores = (0 until p.getValue("count")).map { p.getValue("s$it") }
                assertTrue(scores.sum() % scores.size == 0)
                (scores.sum() / scores.size).toString()
            }
        }
    }

    @Test
    fun everyDrillAnswerIsRight() {
        for (kind in DrillKind.entries) {
            for (seed in 0L until 1000L) {
                val d = DrillGenerator.make(kind, seed)
                assertEquals(expected(d), d.question.answer, "${kind.name} seed $seed: ${d.question.prompt}")
                assertTrue(AnswerChecker.isCorrect(d.question, d.question.answer), "${kind.name} seed $seed")
                assertTrue(d.question.prompt.isNotBlank() && d.question.solution.isNotBlank() && d.question.hint.isNotBlank())
                assertTrue(d.question.answer.toIntOrNull()?.let { it >= 0 } ?: (kind == DrillKind.WEEKDAY))
            }
        }
    }

    @Test
    fun sameSeedSameQuestion() {
        assertEquals(DrillGenerator.random(7).question, DrillGenerator.random(7).question)
    }

    @Test
    fun helpers() {
        assertEquals("21st", DrillGenerator.ordinal(21))
        assertEquals("12th", DrillGenerator.ordinal(12))
        assertEquals("12:05 PM", DrillGenerator.clock(12 * 60 + 5))
        assertEquals("12:00 AM", DrillGenerator.clock(0))
    }
}
