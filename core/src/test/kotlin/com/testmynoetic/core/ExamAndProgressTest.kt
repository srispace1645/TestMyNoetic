package com.testmynoetic.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExamAndProgressTest {

    private fun q(id: String, topic: Topic, difficulty: Int, answer: String = "1") = Question(
        id = id, topic = topic, skill = "", difficulty = difficulty, prompt = "",
        answer = answer, hint = "", solution = "",
    )

    private val pool = Topic.entries.flatMap { t -> (1..12).map { i -> q("${t.name}-$i", t, 1 + i % 3) } }

    @Test
    fun gradingGivesFivePointsPerQuestion() {
        val qs = listOf(q("a", Topic.NUMBER, 1, "5"), q("b", Topic.LOGIC, 2, "7"), q("c", Topic.LOGIC, 3, "9"))
        val result = gradeExam(qs, mapOf(0 to "5", 1 to "8"))
        assertEquals(1, result.correct)
        assertEquals(5, result.score)
        assertEquals(15, result.maxScore)
        assertEquals(listOf(true, false, false), result.perQuestion)
        assertEquals(TopicScore(0, 2), result.byTopic[Topic.LOGIC])
        assertTrue(Topic.GEOMETRY !in result.byTopic)
    }

    @Test
    fun shuffleTestIsBalancedOrderedAndRepeatable() {
        val test = buildShuffleTest(pool, seed = 42)
        assertEquals(20, test.size)
        assertEquals(20, test.map { it.id }.toSet().size)
        Topic.entries.forEach { t -> assertEquals(5, test.count { it.topic == t }) }
        assertEquals(test.map { it.difficulty }.sorted(), test.map { it.difficulty })
        assertEquals(test, buildShuffleTest(pool, seed = 42))
    }

    @Test
    fun mistakesLeaveReviewAfterTwoRightInARow() {
        val question = q("x", Topic.ALGEBRA, 2)
        var p = Progress().record(question, correct = false)
        assertEquals(listOf("x"), p.mistakeIds())
        p = p.record(question, correct = true)
        assertEquals(listOf("x"), p.mistakeIds())
        p = p.record(question, correct = true)
        assertTrue(p.mistakeIds().isEmpty())
        assertEquals(66, p.accuracy(Topic.ALGEBRA))
    }

    @Test
    fun progressSurvivesJsonRoundTrip() {
        val qs = pool.take(4)
        val result = gradeExam(qs, mapOf(0 to "1", 1 to "2"))
        val p = Progress().recordMock("Set 1", "2026-10-04", qs, result).copy(bestDrillStreak = 9)
        assertEquals(p, Progress.fromJson(p.toJson()))
        assertEquals(Progress(), Progress.fromJson("not json"))
        assertEquals(Progress(), Progress.fromJson(null))
    }
}
