package com.testmynoetic.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChoicesTest {

    private val bank = QuestionBank.loadAll().single { it.info.id == "grade4_fall" }
    private val time = Regex("""^\d{1,2}:\d{2} [AP]M$""")

    private fun checkOptions(q: Question) {
        val options = ChoiceMaker.optionsFor(q)
        val where = "${q.id} ${q.prompt.take(40)} -> $options"
        if (q.kind == AnswerKind.NUMBER) assertEquals(4, options.size, where) else assertTrue(options.size in 3..4, where)
        assertEquals(1, options.count { AnswerChecker.isCorrect(q, it) }, "exactly one right answer: $where")
        val keys = options.map { if (q.kind == AnswerKind.NUMBER) AnswerChecker.parseNumber(it) else AnswerChecker.normalizeWord(it) }
        assertEquals(keys.size, keys.toSet().size, "options must all differ: $where")
        assertEquals(options, ChoiceMaker.optionsFor(q), "same order every time: $where")
        if (q.kind == AnswerKind.NUMBER) {
            options.forEach { assertTrue(AnswerChecker.parseNumber(it)!!.num >= 0, "no negatives: $where") }
            when {
                '/' in q.answer -> assertTrue(options.all { '/' in it }, "fractions stay fractions: $where")
                '.' in q.answer -> assertTrue(options.all { '.' in it }, "decimals stay decimals: $where")
                else -> assertTrue(options.all { it.toLongOrNull() != null }, "whole numbers stay whole: $where")
            }
        } else if (time.matches(q.answer)) {
            assertTrue(options.all { time.matches(it) }, "times stay times: $where")
        }
    }

    @Test
    fun everyBankQuestionHasGoodOptions() = bank.allQuestions.forEach(::checkOptions)

    @Test
    fun everyDrillHasGoodOptions() {
        for (kind in DrillKind.entries) for (seed in 0L until 1000L) checkOptions(DrillGenerator.make(kind, seed).question)
    }

    @Test
    fun rightAnswerMovesAroundThePositions() {
        val fourChoice = bank.allQuestions.map { it to ChoiceMaker.optionsFor(it) }.filter { it.second.size == 4 }
        val counts = IntArray(4)
        fourChoice.forEach { (q, options) -> counts[options.indexOfFirst { AnswerChecker.isCorrect(q, it) }]++ }
        counts.forEachIndexed { pos, n ->
            val share = n.toDouble() / fourChoice.size
            assertTrue(share in 0.15..0.35, "answer is in position ${'A' + pos} ${(share * 100).toInt()}% of the time: ${counts.toList()}")
        }
    }

    @Test
    fun rightAnswerIsNotAlwaysTheMiddleNumber() {
        val numbers = bank.allQuestions.filter { it.kind == AnswerKind.NUMBER }
        val atEdge = numbers.count { q ->
            val values = ChoiceMaker.optionsFor(q).map { AnswerChecker.parseNumber(it)!!.let { r -> r.num.toDouble() / r.den } }
            val answer = AnswerChecker.parseNumber(q.answer)!!.let { r -> r.num.toDouble() / r.den }
            answer == values.max() || answer == values.min()
        }
        val share = atEdge.toDouble() / numbers.size
        assertTrue(share in 0.3..0.7, "answer is the biggest or smallest option ${(share * 100).toInt()}% of the time")
    }

    @Test
    fun labelsShowUnits() {
        val q = bank.allQuestions.first { it.unitBefore == "$" }
        assertEquals("$${q.answer}", ChoiceMaker.label(q, q.answer))
        val feet = bank.allQuestions.first { it.unit == "feet" }
        assertEquals("${feet.answer} feet", ChoiceMaker.label(feet, feet.answer))
    }
}
