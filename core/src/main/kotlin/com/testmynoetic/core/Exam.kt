package com.testmynoetic.core

import kotlin.random.Random

const val POINTS_PER_QUESTION = 5
const val CONTEST_SECONDS = 45 * 60

data class TopicScore(val correct: Int, val total: Int)

data class ExamResult(
    val correct: Int,
    val total: Int,
    val perQuestion: List<Boolean>,
    val byTopic: Map<Topic, TopicScore>,
) {
    val score: Int get() = correct * POINTS_PER_QUESTION
    val maxScore: Int get() = total * POINTS_PER_QUESTION
}

/** Grades a finished test. A blank answer is simply wrong, like on the real contest. */
fun gradeExam(questions: List<Question>, answers: Map<Int, String>): ExamResult {
    val marks = questions.mapIndexed { i, q -> AnswerChecker.isCorrect(q, answers[i].orEmpty()) }
    val byTopic = Topic.entries.associateWith { topic ->
        val idx = questions.indices.filter { questions[it].topic == topic }
        TopicScore(idx.count { marks[it] }, idx.size)
    }.filterValues { it.total > 0 }
    return ExamResult(marks.count { it }, questions.size, marks, byTopic)
}

/**
 * Builds a fresh 20-question test from the whole bank: five per topic, one of
 * each difficulty plus two more, then ordered easy to hard like the real paper.
 */
fun buildShuffleTest(bank: List<Question>, seed: Long, perTopic: Int = 5): List<Question> {
    val rnd = Random(seed)
    val picked = mutableListOf<Question>()
    for (topic in Topic.entries) {
        val pool = bank.filter { it.topic == topic }.shuffled(rnd).toMutableList()
        val chosen = mutableListOf<Question>()
        for (d in 1..3) {
            pool.firstOrNull { it.difficulty == d }?.let { chosen += it; pool.remove(it) }
        }
        while (chosen.size < perTopic && pool.isNotEmpty()) chosen += pool.removeAt(0)
        picked += chosen
    }
    return picked.shuffled(rnd).sortedBy { it.difficulty }
}
