package com.testmynoetic.core

import com.testmynoetic.core.verify.set01
import com.testmynoetic.core.verify.set02
import com.testmynoetic.core.verify.set03
import com.testmynoetic.core.verify.set04
import com.testmynoetic.core.verify.set05
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class BankTest {

    private val banks = QuestionBank.loadAll()
    private val bank = banks.single { it.info.id == "grade4_fall" }

    /** Independent solutions, keyed by set number then question number. */
    private val checks: Map<Int, Map<Int, () -> Any>> = mapOf(
        1 to set01,
        2 to set02,
        3 to set03,
        4 to set04,
        5 to set05,
    )

    @Test
    fun everyAnswerMatchesAnIndependentSolution() {
        val problems = mutableListOf<String>()
        bank.sets.forEachIndexed { s, set ->
            val setChecks = checks[s + 1] ?: fail("No checks for ${set.id}")
            set.questions.forEachIndexed { i, q ->
                val check = setChecks[i + 1]
                if (check == null) {
                    problems += "${q.id}: no independent check"
                } else {
                    val computed = check().toString()
                    if (!AnswerChecker.isCorrect(q, computed)) problems += "${q.id}: key says ${q.answer} but check got $computed"
                }
            }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    @Test
    fun setsHaveTheRealContestShape() {
        assertEquals(bank.info.sets.size, bank.sets.size)
        bank.sets.forEachIndexed { s, set ->
            assertEquals(20, set.questions.size, set.id)
            assertEquals("g4f-s%02d".format(s + 1), set.id)
            Topic.entries.forEach { t -> assertEquals(5, set.questions.count { it.topic == t }, "${set.id} $t") }
            val diffs = set.questions.map { it.difficulty }
            assertEquals(diffs.sorted(), diffs, "${set.id} should go from easy to hard")
            assertEquals(1, diffs.first(), set.id)
            assertEquals(3, diffs.last(), set.id)
        }
    }

    @Test
    fun everyQuestionIsComplete() {
        val ids = bank.allQuestions.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "duplicate ids")
        bank.sets.forEach { set ->
            set.questions.forEachIndexed { i, q ->
                assertEquals("%s-q%02d".format(set.id, i + 1), q.id)
                assertTrue(q.difficulty in 1..3, q.id)
                assertTrue(q.prompt.isNotBlank() && q.hint.isNotBlank() && q.solution.isNotBlank() && q.skill.isNotBlank(), q.id)
                assertTrue(AnswerChecker.isCorrect(q, q.answer), "${q.id} rejects its own answer")
                q.accept.forEach { assertTrue(AnswerChecker.isCorrect(q, it), "${q.id} rejects accepted $it") }
                if (q.kind == AnswerKind.NUMBER) assertNotNull(Rational.parse(q.answer), "${q.id} answer is not a number")
                when (val f = q.figure) {
                    is Figure.Pyramid -> assertTrue(f.rows.flatten().contains("?") && f.rows.withIndex().all { (r, row) -> row.size == r + 1 }, q.id)
                    is Figure.Shape -> assertTrue(f.points.size >= 3 && f.sideLabels.keys.all { it in f.points.indices }, q.id)
                    is Figure.BarChart -> assertEquals(f.labels.size, f.values.size, q.id)
                    is Figure.Table -> assertTrue(f.rows.all { it.size == f.header.size }, q.id)
                    is Figure.Grid -> assertTrue(f.shaded.all { it in 0 until f.rows * f.cols }, q.id)
                    else -> {}
                }
            }
        }
    }

    @Test
    fun countdownToContest() {
        assertEquals(39, bank.daysUntilContest(java.time.LocalDate.of(2026, 10, 4)))
        assertEquals(0, bank.daysUntilContest(java.time.LocalDate.of(2026, 11, 20)))
        assertTrue(bank.daysUntilContest(java.time.LocalDate.of(2026, 12, 1)) < 0)
    }
}
