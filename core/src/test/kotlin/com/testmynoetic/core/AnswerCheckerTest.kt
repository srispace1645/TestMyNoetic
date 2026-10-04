package com.testmynoetic.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnswerCheckerTest {

    private fun num(answer: String, vararg accept: String) = Question(
        id = "t", topic = Topic.NUMBER, skill = "", difficulty = 1, prompt = "",
        answer = answer, accept = accept.toList(), hint = "", solution = "",
    )

    private fun word(answer: String, vararg accept: String) =
        num(answer, *accept).copy(kind = AnswerKind.WORD)

    @Test
    fun wholeNumbersIgnoreFormatting() {
        val q = num("1250")
        listOf("1250", " 1250 ", "1,250", "$1,250", "1250.", "1250.0", "1250 cats", "1250 sq cm").forEach {
            assertTrue(AnswerChecker.isCorrect(q, it), "should accept '$it'")
        }
        listOf("125", "12500", "", "abc", "1250/2").forEach {
            assertFalse(AnswerChecker.isCorrect(q, it), "should reject '$it'")
        }
    }

    @Test
    fun fractionsAndDecimalsCompareByValue() {
        val q = num("3/4")
        listOf("3/4", "6/8", "0.75", ".75", "0.750").forEach { assertTrue(AnswerChecker.isCorrect(q, it), it) }
        assertFalse(AnswerChecker.isCorrect(q, "4/3"))
        assertTrue(AnswerChecker.isCorrect(num("1 1/2"), "3/2"))
        assertTrue(AnswerChecker.isCorrect(num("1.5"), "1 1/2"))
    }

    @Test
    fun moneyAndCents() {
        assertTrue(AnswerChecker.isCorrect(num("2.5"), "$2.50"))
        assertTrue(AnswerChecker.isCorrect(num("45"), "45¢"))
    }

    @Test
    fun wordAnswersIgnoreCaseAndSpaces() {
        val q = word("Saturday", "Sat")
        listOf("saturday", "SATURDAY", " Saturday. ", "sat").forEach { assertTrue(AnswerChecker.isCorrect(q, it), it) }
        assertFalse(AnswerChecker.isCorrect(q, "Sunday"))
        assertFalse(AnswerChecker.isCorrect(q, ""))
        assertTrue(AnswerChecker.isCorrect(word("6:55 PM"), "6:55pm"))
        assertTrue(AnswerChecker.isCorrect(word("6:55 PM"), "6:55 p.m."))
    }

    @Test
    fun parseNumberEdgeCases() {
        assertEquals(Rational.of(12), AnswerChecker.parseNumber("12 years old"))
        assertEquals(Rational.of(-3), AnswerChecker.parseNumber("-3"))
        assertEquals(Rational.of(11), AnswerChecker.parseNumber("Day 11"))
        assertNull(AnswerChecker.parseNumber("seven"))
        assertNull(AnswerChecker.parseNumber("3/0"))
        assertNull(AnswerChecker.parseNumber("."))
    }
}
